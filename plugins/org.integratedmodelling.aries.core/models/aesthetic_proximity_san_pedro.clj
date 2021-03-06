;;Proximity model for San Pedro
(ns core.models.aesthetic-proximity-san-pedro
  (:refer-clojure :rename {count length})
  (:refer tl        :only [is? conc])
  (:refer modelling :only [defscenario defmodel measurement classification categorization 
                           namespace-ontology ranking model
                           probabilistic-measurement probabilistic-classification probabilistic-ranking
                           numeric-coding binary-coding identification bayesian count])
  (:refer aries :only [span]))

(namespace-ontology aestheticService)

;; ----------------------------------------------------------------------------------------------
;; Source model
;; ----------------------------------------------------------------------------------------------

;;Data on land cover types are for the U.S. only, using SWReGAP data.  Since we have no parcel/housing location
;; data for Mexico, plan to solely run model in the U.S. on U.S. LULC types.
;; Bare rock/dune, etc. are not included here
(defmodel forest sanPedro:ForestAndWoodland
  (classification (numeric-coding sanPedro:SouthwestRegionalGapAnalysisLULC) 
                  #{22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 41 45 46 63 64 91 92 95 101 102 103} sanPedro:ForestOrWoodlandPresent               
                  :otherwise                                                                                sanPedro:ForestOrWoodlandAbsent))

(defmodel farmland Farmland
  (classification (numeric-coding sanPedro:SouthwestRegionalGapAnalysisLULC)
                  114          FarmlandPresent                     
                  :otherwise   FarmlandAbsent))

(defmodel grassland Grassland
  (classification (numeric-coding sanPedro:SouthwestRegionalGapAnalysisLULC)
                  #{65 68 73 74 75 76 90 93 106}  GrasslandPresent                     
                  :otherwise                      GrasslandAbsent))

(defmodel desert-scrub DesertScrub ;; includes chapparal and other shrubland
  (classification (numeric-coding sanPedro:SouthwestRegionalGapAnalysisLULC)
                  #{40 44 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 66 67 82 94 96 97 105 108}  DesertScrubPresent                  
                  :otherwise                                                                          DesertScrubAbsent))

;;This model assumes that all riparian areas that are not mapped within the SPRNCA are low quality.  This is a poor assumption -
;; moderate quality might also be appropriate and it would be better to run these as a simple BN for presence and quality like
;; the housing presence and value BNs, incoprorating priors for quality when we lack data.
(defmodel riparian-wetland-code sanPedro:RiparianAndWetlandCode
  (numeric-coding sanPedro:RiparianAndWetlandCode
                  :context ((numeric-coding sanPedro:SouthwestRegionalGapAnalysisLULC :as lulc)
                            (ranking sanPedro:RiparianConditionClass :as condition))
                  :state   #(if (contains? #{77.0 78.0 79.0 80.0 81.0 83.0 84.0 85.0 98.0 109.0 110.0 118.0} (:lulc %))
                                (let [condition (:condition %)]
                                  (if (or (nil? condition) (Double/isNaN condition))
                                    1
                                    condition))
                                0)))
(defmodel riparian-wetland sanPedro:RiparianAndWetland
  (classification riparian-wetland-code
                  3 sanPedro:HighQualityRiparianOrWetlandPresent
                  2 sanPedro:ModerateQualityRiparianOrWetlandPresent
                  1 sanPedro:LowQualityRiparianOrWetlandPresent
                  0 sanPedro:RiparianOrWetlandAbsent))

(defmodel park Park
    (classification (numeric-coding habitat:LandOwnership)
     #{8 101 102 103 104 105 106 107 108 109 110 111 112 113 114 115 116 
       117 118 119 120 121 122 123 124 125 126 127}   ParkPresent
     :otherwise                                       ParkAbsent)) 

(defmodel fire-threat FireThreat
  (classification (numeric-coding habitat:FireReturnInterval) 
                  #{1 2 3}        HighFireThreat ;; includes high, moderate, variable fire frequency
                  #{4 5 6}        LowFireThreat))

;;This uses the WDPA data - need to double check that the numbers correspond to protected/not protected (see what's getting output).
;; Might be worthwhile to replace with local data.
;; Should also change unprotected land to state & private land only: other BLM for instance isn't going to be developed.
(defmodel formal-protection FormalProtection
  (classification (binary-coding conservation:ProtectedStatus)
                  1            Protected
                  :otherwise   NotProtected)) 

;; Computes area of open space polygons as a GIS operation and stores this value in each pixel
(defmodel area OpenSpaceAreaClass
  (classification (measurement OpenSpaceArea "ha")
                  [40 :>] VeryLargeArea
                  [10 40] LargeArea
                  [2 10]  SmallArea
                  [:< 2]  VerySmallArea))

(defmodel theoretical-open-space TheoreticalProximitySource
  (probabilistic-ranking TheoreticalProximitySource
                  [0   10] NoProximityPotential 
                  [10  40] LowProximityPotential 
                  [40  75] ModerateProximityPotential 
                  [75 100] HighProximityPotential))

;; source bayesian model	    		 
(defmodel source AestheticProximityProvision
  "This one will harmonize the context, then retrieve and run the BN with the given
   evidence, and produce a new observation with distributions for the requested nodes."
  (bayesian AestheticProximityProvision
            :import   "aries.core::ProximitySourceSanPedro.xdsl"
            :context  (forest farmland grassland desert-scrub park fire-threat formal-protection riparian-wetland area) 
            :result   theoretical-open-space
            :keep     (TheoreticalProximitySource)))

;; ----------------------------------------------------------------------------------------------
;; Sink model
;; ----------------------------------------------------------------------------------------------

(defmodel sink ProximitySink
  (ranking ProximitySink
           :context ((binary-coding infrastructure:Highway :as highway))
           :state #(cond (== (:highway %) 1) 50   ;; 50 units of proximity value are depleted by the sink if highways are present
                         :otherwise          0))) ;; Otherwise zero sink

;; ----------------------------------------------------------------------------------------------
;; Use model
;; ----------------------------------------------------------------------------------------------

(defmodel housing PresenceOfHousing
  (classification (ranking economics:AppraisedPropertyValue)
        [1 :>]       HousingPresent
        :otherwise   HousingAbsent)
  (classification (numeric-coding nlcd:NLCDNumeric) ;;Using NLCD where parcel data are unavailable.
        [22 23 24]   HousingPresent  ;;Assumes (incorrectly) that all developed land is housing.
        :otherwise   HousingAbsent))

(defmodel property-value HousingValue  ;; value is in $/ac, which is not a legitimate unit in thinklab, so kept as a ranking for now.
  (classification (ranking economics:AppraisedPropertyValue)
                  [0         10000] VeryLowHousingValue
                  [10000    25000]  LowHousingValue
                  [25000   50000]   ModerateHousingValue
                  [50000  200000]   HighHousingValue
                  [200000 :>]       VeryHighHousingValue))

;;Urban proximity proxied by year 2000 population density for Arizona
(defmodel urban-proximity UrbanProximity
  (classification (count policytarget:PopulationDensity "/km^2")
                  [309 :>] Urban
                  [77 309] Suburban
                  [:< 77]  Rural))

;;undiscretizer for proximty use
(defmodel proximity-use-undiscretizer HomeownerProximityUse
  (probabilistic-ranking HomeownerProximityUse
                  [0 0.05] HomeownerProximityUseAbsent
                  [0.05 1] HomeownerProximityUsePresent))

;; bayesian model
(defmodel homeowners ProximityUse
  "Property owners who can afford to pay for proximity to open space"
  (bayesian ProximityUse 
            :import  "aries.core::ProximityUseSanPedro.xdsl"
            :context (urban-proximity property-value housing) 
            :result  proximity-use-undiscretizer 
            :keep    (HomeownerProximityUse)))						

;; ---------------------------------------------------------------------------------------------------	 	 	
;; Top-level service models 
;; ---------------------------------------------------------------------------------------------------	 	 	

;;Gary: For the flow models, distance decay on proxmity should usually be steep (i.e., very steep after 0.5 mi, almost
;; nothing left after 1.0 mi.  However, this decay should be a little less steep for rivers in western regions (i.e.,
;; San Pedro) - perhaps a similar decay function stretched with an inflection point at 1.0 mi and decaying to very little
;; at 2.0 mi.

;; all data, for testing and storage
(defmodel data Proximity
  (identification Proximity
                  :context (source     :as source
                            homeowners :as use
                            sink       :as sink)))

(defmodel proximity AestheticProximity
  (span Proximity
        AestheticProximityProvision
        ProximityUse
        ProximitySink
        nil
        nil
        :source-threshold   40.0  ;; Excludes LowProximityPotential
        :sink-threshold     0.0   ;; Deterministic as 0.0 or 50.0 based on presence of highways
        :use-threshold      0.2   ;; Excludes HomeownerProximityUseAbsent
        :trans-threshold    1.0
        :source-type        :infinite
        :sink-type          :infinite
        :use-type           :infinite
        :benefit-type       :non-rival
        :downscaling-factor 1
        :rv-max-states      10
        :animation?         true
        ;;:save-file          (str (System/getProperty "user.home") "/aesthetic_proximity_san_pedro_data.clj")
        :keep (PotentialProximateOpenSpace
               PotentialProximitySink
               HomeownersWithOpenSpaceDemand
               PossibleProximateOpenSpace
               AccessibleOpenSpace
               OpenSpaceProximateHomeowners
               AccessibleProximity
               EnjoyedOpenSpace
               BlockingProximitySink
               HomeownersWithProximateOpenSpace
               UnaccessedOpenSpace
               InaccessibleProximitySink
               HomeownersWithoutProximateOpenSpace
               BlockedProximity
               BlockedOpenSpace
               HomeownersWithBlockedProximity)
        :context (source homeowners sink)))

;; ----------------------------------------------------------------------------------------------
;; Scenarios 

;; Observations that are specifically tagged for a scenario will be picked up automatically
;; instead of the baseline ones.
;; ----------------------------------------------------------------------------------------------

(defscenario cap-water-augmentation-half-meter-rise
"Water augmentation leading to a 0.5 meter rise across the San Pedro Riparian National Conservation Area" 
  (model sanPedro:RiparianConditionClass
         (ranking sanPedro:RiparianConditionClassHalfMeterRise)))

(defscenario cap-water-augmentation-all-perennial
"Water augmentation leading to perennial flow conditions across the San Pedro Riparian National Conservation Area" 
  (model sanPedro:RiparianConditionClass
         (ranking sanPedro:RiparianConditionClassAllWet)))

(defmodel constrained-development-scenario sanPedro:ConstrainedDevelopment
  (classification (numeric-coding sanPedro:Steinitz30ClassUrbanGrowthLULCConstrained) 
      #{10 11 12 13 19 22 25}                      sanPedro:DevelopedConstrained
      #{1 2 4 5 6 7 8 9 14 16 23 26 27 28}         sanPedro:NotDevelopedConstrained))

(defmodel open-development-scenario sanPedro:OpenDevelopment
  (classification (numeric-coding sanPedro:Steinitz30ClassUrbanGrowthLULCOpen) 
      #{10 11 12 13 19 22 25}                      sanPedro:DevelopedOpen
      #{1 2 4 5 6 7 8 9 14 16 23 26 27 28 29}      sanPedro:NotDevelopedOpen))

(defscenario open-development-proximity
  "Changes values in developed areas to no valuable open space type, fire threat to low, high housing value present."
  (model sanPedro:ForestAndWoodland
    (classification sanPedro:ModifiedForestAndWoodland
        :context (open-development-scenario forest)
        :state #(if (is? (:open-development %) (conc 'sanPedro:DevelopedOpen))
                  (conc 'sanPedro:ForestOrWoodlandAbsent)
                  (:forest-and-woodland %))))
  (model Farmland
    (classification ModifiedFarmland
        :context (open-development-scenario farmland)
        :state #(if (is? (:open-development %) (conc 'sanPedro:DevelopedOpen))
                  (conc 'FarmlandAbsent)  ;;Might have to add "aestheticService"
                  (:farmland %))))
  (model Grassland
    (classification ModifiedGrassland
        :context (open-development-scenario grassland)
        :state #(if (is? (:open-development %) (conc 'sanPedro:DevelopedOpen))
                  (conc 'GrasslandAbsent)  ;;Might have to add "aestheticService"
                  (:grassland %))))
  (model DesertScrub
    (classification ModifiedDesertScrub
        :context (open-development-scenario desert-scrub)
        :state #(if (is? (:open-development %) (conc 'sanPedro:DevelopedOpen))
                  (conc 'DesertScrubAbsent)  ;;Might have to add "aestheticService"
                  (:desert-scrub %))))
  (model Park
    (classification ModifiedPark
        :context (open-development-scenario park)
        :state #(if (is? (:open-development %) (conc 'sanPedro:DevelopedOpen))
                  (conc 'ParkAbsent)  ;;Might have to add "aestheticService"
                  (:park %))))
  (model sanPedro:RiparianAndWetland
    (classification sanPedro:ModifiedRiparianAndWetland
        :context (open-development-scenario riparian-wetland)
        :state #(if (is? (:open-development %) (conc 'sanPedro:DevelopedOpen))
                  (conc 'sanPedro:RiparianOrWetlandAbsent)
                  (:riparian-and-wetland %))))
  (model FireThreat
    (classification ModifiedFireThreat
        :context (open-development-scenario fire-threat)
        :state #(if (is? (:open-development %) (conc 'sanPedro:DevelopedOpen))
                  (conc 'LowFireThreat)     ;;Might have to add "aestheticService" in between the tick and LowFireThreat
                  (:fire-threat %))))
  (model PresenceOfHousing
    (classification ModifiedPresenceOfHousing
        :context (open-development-scenario housing)
        :state #(if (is? (:open-development %) (conc 'sanPedro:DevelopedOpen))
                  (conc 'HousingPresent)            ;;Might have to add "aestheticService" in between the tick and HousingPresent
                  (:presence-of-housing %))))
  (model HousingValue
    (classification ModifiedHousingValue
        :context (open-development-scenario property-value)
        :state #(if (is? (:open-development %) (conc 'sanPedro:DevelopedOpen))
                  (conc 'HighHousingValue)            ;;Might have to add "aestheticService" in between the tick and HighHousingValue
                  (:housing-value %)))))

(defscenario constrained-development-proximity
  "Changes values in developed areas to no valuable open space type, fire threat to low, high housing value present."
  (model sanPedro:ForestAndWoodland
    (classification sanPedro:ModifiedForestAndWoodland
        :context (constrained-development-scenario forest)
        :state #(if (is? (:constrained-development %) (conc 'sanPedro:DevelopedConstrained))
                  (conc 'sanPedro:ForestOrWoodlandAbsent)
                  (:forest-and-woodland %))))
  (model Farmland
    (classification ModifiedFarmland
        :context (constrained-development-scenario farmland)
        :state #(if (is? (:constrained-development %) (conc 'sanPedro:DevelopedConstrained))
                  (conc 'FarmlandAbsent)  ;;Might have to add "aestheticService"
                  (:farmland %))))
  (model Grassland
    (classification ModifiedGrassland
        :context (constrained-development-scenario grassland)
        :state #(if (is? (:constrained-development %) (conc 'sanPedro:DevelopedConstrained))
                  (conc 'GrasslandAbsent)  ;;Might have to add "aestheticService"
                  (:grassland %))))
  (model DesertScrub
    (classification ModifiedDesertScrub
        :context (constrained-development-scenario desert-scrub)
        :state #(if (is? (:constrained-development %) (conc 'sanPedro:DevelopedConstrained))
                  (conc 'DesertScrubAbsent)  ;;Might have to add "aestheticService"
                  (:desert-scrub %))))
  (model Park
    (classification ModifiedPark
        :context (constrained-development-scenario park)
        :state #(if (is? (:constrained-development %) (conc 'sanPedro:DevelopedConstrained))
                  (conc 'ParkAbsent)  ;;Might have to add "aestheticService"
                  (:park %))))
  (model sanPedro:RiparianAndWetland
    (classification sanPedro:ModifiedRiparianAndWetland
        :context (constrained-development-scenario riparian-wetland)
        :state #(if (is? (:constrained-development %) (conc 'sanPedro:DevelopedConstrained))
                  (conc 'sanPedro:RiparianOrWetlandAbsent)
                  (:riparian-and-wetland %))))
  (model FireThreat
    (classification ModifiedFireThreat
        :context (constrained-development-scenario fire-threat)
        :state #(if (is? (:constrained-development %) (conc 'sanPedro:DevelopedConstrained))
                  (conc 'LowFireThreat)     ;;Might have to add "aestheticService" in between the tick and LowFireThreat
                  (:fire-threat %))))
  (model PresenceOfHousing
    (classification ModifiedPresenceOfHousing
        :context (constrained-development-scenario housing)
        :state #(if (is? (:constrained-development %) (conc 'sanPedro:DevelopedConstrained))
                  (conc 'HousingPresent)            ;;Might have to add "aestheticService" in between the tick and HousingPresent
                  (:presence-of-housing %))))
  (model HousingValue
    (classification ModifiedHousingValue
        :context (constrained-development-scenario property-value)
        :state #(if (is? (:constrained-development %) (conc 'sanPedro:DevelopedConstrained))
                  (conc 'HighHousingValue)            ;;Might have to add "aestheticService" in between the tick and HighHousingValue
                  (:housing-value %)))))