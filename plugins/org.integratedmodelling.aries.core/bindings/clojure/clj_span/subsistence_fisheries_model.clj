;;; Copyright 2010 Gary Johnson
;;;
;;; This file is part of clj-span.
;;;
;;; clj-span is free software: you can redistribute it and/or modify
;;; it under the terms of the GNU General Public License as published
;;; by the Free Software Foundation, either version 3 of the License,
;;; or (at your option) any later version.
;;;
;;; clj-span is distributed in the hope that it will be useful, but
;;; WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;;; General Public License for more details.
;;;
;;; You should have received a copy of the GNU General Public License
;;; along with clj-span.  If not, see <http://www.gnu.org/licenses/>.
;;;
;;;-------------------------------------------------------------------
;;;
;;; This namespace defines the subsistence-fisheries model.
;;;
;;; Procedure:
;;; 0. Setup source caches at all source points.
;;; 1. For each use point, find its nearest path point and the closest shore point.
;;; 2. Follow the path, using a closed list and the shore orientation as a shortest-distance heuristic.
;;; 3. Make a map of these shore paths for each use point.
;;; 4. Enter the water and start depleting the source caches.
;;; 5. A fisherman will continue to work in the same area until either the source cache runs out or his demand is met.
;;; 6. If the fisherman still has unmet demand after his source cache is depleted,
;;;    radiate into all neighboring, fish-producing pixels, split the demand evenly (naive, I know), and continue.
;;; 7. Once all demand is met, end the simulation and return the cache-layer.

(ns clj-span.subsistence-fisheries-model
  (:use [clj-span.core       :only (distribute-flow! service-carrier)]
        [clj-misc.utils      :only (p
                                    &
                                    seq2map
                                    count-distinct
                                    angular-distance
                                    with-message
                                    with-progress-bar-cool
                                    iterate-while-seq
                                    shortest-path)]
        [clj-misc.matrix-ops :only (subtract-ids
                                    filter-matrix-for-coords
                                    get-neighbors
                                    get-bearing
                                    find-nearest
                                    find-in-range
                                    find-line-between)]
        [clj-misc.randvars   :only (_0_ *_ _d _*_ _+_ rv-fn rv-zero-ish?)]))

(defstruct fisherman :need :route :cache :fishing-area)

(defn go-fish!
  [fish-supply fish-left? possible-flow-layer actual-flow-layer km2-per-cell
   {:keys [need route cache fishing-area] :as fisherman}]
  (dosync
   (let [current-id       (peek route)
         local-supply-ref (fish-supply current-id)
         local-supply     (deref local-supply-ref)
         need-remaining   (if-not (rv-zero-ish? 0.05 local-supply) ;; Check for fish locally.
                            ;; There are fish here. Let's get some.
                            (let [fish-caught         (rv-fn min local-supply need)
                                  fish-remaining      (rv-fn (fn [s n] (- s (min s n))) local-supply need)
                                  need-remaining      (rv-fn (fn [s n] (- n (min s n))) local-supply need)
                                  fish-caught-per-km2 (_d fish-caught km2-per-cell)]
                              ;; Reduce the local-supply-ref by this amount.
                              (alter local-supply-ref (constantly fish-remaining))
                              ;; Update the possible and actual flow layers.
                              (doseq [id route]
                                (alter (get-in possible-flow-layer id) _+_ fish-caught-per-km2)
                                (alter (get-in actual-flow-layer   id) _+_ fish-caught-per-km2))
                              ;; Store a service-carrier in your cache.
                              (alter cache conj (struct-map service-carrier
                                                  :source-id       current-id
                                                  ;;:route           (rseq route) ;; Deprecated
                                                  :possible-weight fish-caught-per-km2
                                                  :actual-weight   fish-caught-per-km2))
                              need-remaining)
                            need)]
     ;; On to the next.
     (if-not (rv-zero-ish? 0.05 need-remaining) ;; to catch those pesky floating-point rounding errors
       (if-let [fishing-area-remaining (seq (filter fish-left? fishing-area))]
         (assoc fisherman
           :need         need-remaining
           :route        (conj (pop route) (first fishing-area-remaining))
           :fishing-area (rest fishing-area-remaining)))))))

(defn send-forth-fishermen!
  [fishermen fish-supply possible-flow-layer actual-flow-layer km2-per-cell]
  (with-message "Let's go fishing...\n" "All done."
    (let [fish-left? #(if-let [supply-ref (fish-supply %)] (not (rv-zero-ish? 0.05 (deref supply-ref))))]
      (dorun (iterate-while-seq
              #(with-message "Fishermen: " count
                 (doall (pmap (p go-fish! fish-supply fish-left? possible-flow-layer actual-flow-layer km2-per-cell) %)))
              fishermen)))))

(def *fishing-range* 5000.0) ;; max distance in meters that a fisherman can sail from shore

;; FIXME: Use locations with no path to the coast will not create fishermen agents.
(defn make-fishermen
  [fishing-spot? use-vals fishing-routes cache-layer cell-width cell-height rows cols]
  (with-message
    "Creating fishermen agents...\n"
    #(str
      "Total fishermen agents:      " (count %) "\n"
      "Distinct launch points:      " (count (distinct (map (& peek :route) %))) "\n"
      "Distinct route lengths:      " (count-distinct (map (& count :route) %)) "\n"
      "Distinct fishing area sizes: " (count-distinct (map (& count :fishing-area) %)))
    (remove nil?
            (map (fn [use-val route]
                   (if route
                     (struct-map fisherman
                       :need         use-val
                       :route        route
                       :cache        (get-in cache-layer (first route))
                       :fishing-area (find-in-range fishing-spot? *fishing-range* cell-width cell-height rows cols (peek route)))))
                 use-vals
                 fishing-routes))))

(defn find-shortest-paths-to-coast
  [path? fishing-spot? rows cols use-points]
  (with-message "Finding paths to coast...\n" "\nAll done."
    (with-progress-bar-cool
      :keep
      (count use-points)
      (map
       #(let [path-root      (find-nearest path?         rows cols %)
              fishing-spot   (find-nearest fishing-spot? rows cols %)
              bearing        (get-bearing path-root fishing-spot)
              follow-path    (fn [id] (filter path? (get-neighbors rows cols id)))
              follow-bearing (fn [id neighbors] (if (seq neighbors)
                                                  (let [neighbor-bearings  (map (fn [nid] (subtract-ids nid id)) neighbors)
                                                        bearing-deviations (map (p angular-distance bearing) neighbor-bearings)
                                                        min-deviation      (apply min bearing-deviations)]
                                                    (keys (filter (fn [[_ dev]] (== dev min-deviation))
                                                                  (zipmap neighbors bearing-deviations))))))
              path-ids       (shortest-path path-root follow-path fishing-spot? follow-bearing)]
          (if (seq path-ids)
            (vec (concat (find-line-between % path-root) (rest path-ids)))))
       use-points))))

;; FIXME: Because Theoretical Use is in kg/person*year and
;;        Possible/Actual Use are in kg/km^2*year, Inaccessible Use
;;        will not make sense.
(defmethod distribute-flow! "SubsistenceFishAccessibility"
  [_ cell-width cell-height rows cols cache-layer possible-flow-layer
   actual-flow-layer source-layer _ use-layer source-points _ use-points
   {path-layer "Path", population-density-layer "PopulationDensity"}]
  (let [km2-per-cell   (* cell-width cell-height (Math/pow 10.0 -6.0))
        fish-supply    (seq2map source-points
                                (fn [id] [id (ref (*_ km2-per-cell ;; km^2
                                                      (get-in source-layer id)))])) ;; kg/km^2*year
        use-vals       (map (fn [id] (*_ km2-per-cell ;; km^2
                                         (_*_ (get-in use-layer id) ;; kg/person*year
                                              (get-in population-density-layer id)))) ;; person/km^2
                            use-points)
        path?          (set (filter-matrix-for-coords (p not= _0_) path-layer))
        fishing-spot?  (set source-points)
        fishing-routes (find-shortest-paths-to-coast path? fishing-spot? rows cols use-points)
        fishermen      (make-fishermen fishing-spot? use-vals fishing-routes cache-layer cell-width cell-height rows cols)]
    (send-forth-fishermen! fishermen fish-supply possible-flow-layer actual-flow-layer km2-per-cell)))
