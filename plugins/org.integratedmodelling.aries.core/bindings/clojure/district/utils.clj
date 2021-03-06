;;; Copyright 2009 Gary Johnson
;;;
;;; This file is part of CLJ-DISTRICT.
;;;
;;; CLJ-DISTRICT is free software: you can redistribute it
;;; and/or modify it under the terms of the GNU General Public License
;;; as published by the Free Software Foundation, either version 3 of
;;; the License, or (at your option) any later version.
;;;
;;; CLJ-DISTRICT is distributed in the hope that it will be
;;; useful, but WITHOUT ANY WARRANTY; without even the implied warranty
;;; of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;;; General Public License for more details.
;;;
;;; You should have received a copy of the GNU General Public License
;;; along with CLJ-DISTRICT.  If not, see
;;; <http://www.gnu.org/licenses/>.

(ns district.utils
  (:refer-clojure)
  (:import (java.util HashMap)))

(defn print-sysprops
  "Print out the result of System.getProperties()"
  []
  (doseq [[key val] (. System getProperties)]
      (printf "%s = %s\n" key val)))

(defn seq2map
  "Constructs a map from a sequence by applying keyvalfn to each
   element of the sequence.  keyvalfn should return a pair [key val]
   to be added to the map for each input sequence element."
  [aseq keyvalfn]
  (loop [aseq aseq
	 amap {}]
    (if (empty? aseq)
      amap
      (let [[key val] (keyvalfn (first aseq))]
	(recur (rest aseq)
	       (assoc amap key val))))))

(defn seq2redundant-map
  "Constructs a map from a sequence by applying keyvalfn to each
   element of the sequence.  keyvalfn should return a pair [key val]
   to be added to the map for each input sequence element.  If key is
   already in the map, its current value will be combined with the new
   val using (mergefn curval val)."
  [aseq keyvalfn mergefn]
  (loop [aseq aseq
	 amap {}]
    (if (empty? aseq)
      amap
      (let [[key val] (keyvalfn (first aseq))]
	(recur (rest aseq)
	       (update-in amap [key] mergefn val))))))

(defn maphash
  "Creates a new map by applying keyfn to every key of in-map and
   valfn to every corresponding val."
  [keyfn valfn in-map]
  (seq2map (seq in-map) (fn [[key val]] [(keyfn key) (valfn val)])))

(defn maphash-java
  "Creates a new Java map by applying keyfn to every key of in-map and
   valfn to every corresponding val."
  [keyfn valfn in-map]
  (loop [keyvals (seq in-map)
	 out-map (new HashMap)]
    (if (empty? keyvals)
      out-map
      (let [[key val] (first keyvals)]
	(recur (rest keyvals)
	       (doto out-map (.put (keyfn key) (valfn val))))))))

(defn linearize
  "Transforms a 2D matrix into a 1D vector."
  [matrix]
  (vec (mapcat identity matrix)))

(defn vectorize
  "Creates a vect of vects from a 2D Java array."
  [java-array]
  (into [] (map #(into [] %) java-array)))

(defn arrayify
  "Creates a 2D Java array (of Objects) from a vect of vects."
  [vect-of-vects]
  (into-array (map into-array vect-of-vects)))

(defn vectorize-map
  "Creates a map of {keywords -> vect-of-vects} from a Java
   HashMap<String,Array[]>."
  [java-map]
  (maphash keyword vectorize java-map))

(defn arrayify-map
  "Creates a Java HashMap<String,Array[]> from a map of {keywords ->
   vect-of-vects}."
  [clojure-map]
  (maphash-java name arrayify clojure-map))

(defn multi-conj
  "Conjoins an element multiple times onto a base-coll."
  [element times base-coll]
  (nth (iterate #(conj % element) base-coll) times))

(defn expand-runtime-encoded-vector
  "Expands a vector of the form [:foo 2 :bar 1 :baz 3] into
   [:foo :foo :bar :baz :baz :baz]."
  [avec]
  (assert (even? (count avec)))
  (loop [orig-vec avec
	 expanded-vec []]
    (if (empty? orig-vec)
      expanded-vec
      (recur (drop 2 orig-vec)
	     (multi-conj (first orig-vec) (second orig-vec) expanded-vec)))))

(defn contains-item?
  "Returns true if sequence contains item.  Otherwise nil."
  [sequence item]
  (some #(= % item) sequence))

(defn breadth-first-search
  "The classic breadth-first-search.  Bread and butter of computer
   science.  Implemented using tail recursion, of course! ;)"
  [open-list closed-list successors goal?]
  (when-first [this-node open-list]
      (if (contains-item? closed-list this-node)
	(breadth-first-search (rest open-list) closed-list successors goal?)
	(if (goal? this-node)
	  this-node
	  (breadth-first-search (concat (rest open-list)
					(filter #(not (contains-item? closed-list %))
						(successors this-node)))
				(cons this-node closed-list)
				successors
				goal?)))))

(defn between [val low high] (and (>= val low) (< val high)))
