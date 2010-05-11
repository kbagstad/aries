(ns aries.sediment
  (:refer modelling :only (defmodel measurement classification categorization ranking identification bayesian))
  (:refer aries :only (span)))

;; ----------------------------------------------------------------------------------------------
;; provision model
;; ----------------------------------------------------------------------------------------------

(defmodel soil-group 'soilretentionEcology:HydrologicSoilsGroup
	"Relevant soil group"
	(classification (categorization 'soilretentionEcology:HydrologicSoilsGroup)
			1       'soilretentionEcology:SoilGroupA
			2       'soilretentionEcology:SoilGroupB
			3       'soilretentionEcology:SoilGroupC
			4       'soilretentionEcology:SoilGroupD))

(defmodel slope 'soilretentionEcology:Slope
		(classification (ranking 'geophysics:DegreeSlope)
			 [:< 1.15] 	  'soilretentionEcology:Level
			 [1.15 4.57] 	'soilretentionEcology:GentlyUndulating
			 [4.57 16.70] 'soilretentionEcology:RollingToHilly
			 [16.70 :>] 	'soilretentionEcology:SteeplyDissectedToMountainous))

(defmodel slope-stability 'soilretentionEcology:SlopeStability
    (classification (ranking 'soilretentionEcology:SlopeStability)	 		
      1           'soilretentionEcology:HighSlopeStability
	 		2           'soilretentionEcology:ModerateSlopeStability
	 		3           'soilretentionEcology:LowSlopeStability)) 

(defmodel soil-texture 'soilretentionEcology:SoilTexture
    (classification (categorization 'soilretentionEcology:SoilTexture)
      "Coarse"    'soilretentionEcology:CoarseSoilTexture
      "Medium"    'soilretentionEcology:MediumSoilTexture
      "Fine"      'soilretentionEcology:FineSoilTexture)) 

;;Soil erodibility factor from USLE (unitless).
(defmodel soil-erodibility 'soilretentionEcology:SoilErodibility
     (classification (ranking 'soilretentionEcology:SoilErodibility)
       [:< 0.1]    'soilretentionEcology:VeryLowSoilErodibility
       [0.1 0.225]   'soilretentionEcology:LowSoilErodibility
       [0.225 0.3]   'soilretentionEcology:ModerateSoilErodibility
       [0.3 0.375]   'soilretentionEcology:HighSoilErodibility
       [0.375 :>]     'soilretentionEcology:VeryHighSoilErodibility)

;;Monthly precipitation for Puget Sound.
(defmodel precipitation-monthly 'soilretentionEcology:MonthlyPrecipitation
	"FIXME this is total monthly precipitation."
	(classification (measurement 'soilretentionEcology:MonthlyPrecipitation "in")
		[:< 3] 	  'soilretentionEcology:VeryLowMonthlyPrecipitation
		[3 6] 	  'soilretentionEcology:LowMonthlyPrecipitation
		[6 12] 	  'soilretentionEcology:ModerateMonthlyPrecipitation
		[12 24] 	'soilretentionEcology:HighMonthlyPrecipitation
		[24 :>] 	'soilretentionEcology:VeryHighMonthlyPrecipitation))

;;Annual precipitation for Mg & DR
(defmodel precipitation-annual 'soilretentionEcology:AnnualPrecipitation
	"FIXME this is annual precipitation."
	(classification (measurement 'soilretentionEcology:AnnualPrecipitation "mm")
    [:< 600] 	    'soilretentionEcology:VeryLowAnnualPrecipitation
		[600 1200] 	  'soilretentionEcology:LowAnnualPrecipitation
		[1200 1800]   'soilretentionEcology:ModerateAnnualPrecipitation
		[1800 2200] 	'soilretentionEcology:HighAnnualPrecipitation
		[2200 :>] 	  'soilretentionEcology:VeryHighAnnualPrecipitation))

;; Surface temperature - again, should be monthly and matched by temporal extents.  For Puget Sound only.
(defmodel monthly-temperature 'soilretentionEcology:MonthlyTemperature
		(classification (ranking 'geophysics:GroundSurfaceTemperature "C")
			 [4 :>] 	'soilretentionEcology:HighTemperature
			 [-4 4] 	'soilretentionEcology:ModerateTemperature
			 [:< -4] 	'soilretentionEcology:LowTemperature))

;;Snowmelt, use only in Puget Sound
(defmodel monthly-snowmelt 'soilretentionEcology:MonthlySnowmelt
	(classification (measurement 'soilretentionEcology:MonthlySnowmelt "mm")
		0 	                  'soilretentionEcology:NoMonthlySnowmelt
		[:exclusive 0 50] 	  'soilretentionEcology:LowMonthlySnowmelt
		[50 100] 	            'soilretentionEcology:ModerateMonthlySnowmelt
		[100 :>]            	'soilretentionEcology:HighMonthlySnowmelt))

;;Tropical storm probability, use only in DR & Mg
(defmodel storm-probability 'soilretentionEcology:TropicalStormProbability
 (classification (ranking 'habitat:TropicalStormProbability)
        0     'soilretentionEcology:NoTropicalStormProbability
      [1 5]   'soilretentionEcology:ModerateTropicalStormProbability
      [6 10]  'soilretentionEcology:HighTropicalStormProbability)) 

;;Annual runoff, whereas snowmelt, precipitation, and temperature are monnthly, so this is problematic.
;;Could divide yearly runoff by 12 but obviously it's not evenly distributed throughout the year.
(defmodel runoff 'soilretentionEcology:AnnualRunoff
	(classification (measurement 'soilretentionEcology:AnnualRunoff "mm")
		[0 200] 	    'soilretentionEcology:VeryLowAnnualRunoff
		[200 600] 	  'soilretentionEcology:LowAnnualRunoff
		[600 1200]  	'soilretentionEcology:ModerateAnnualRunoff
		[1200 2400] 	'soilretentionEcology:HighAnnualRunoff
		[2400 :>] 	  'soilretentionEcology:VeryHighAnnualRunoff))

;;Vegetation type
(defmodel vegetation-type 'soilretentionEcology:VegetationType
	"Just a reclass of the NLCD land use layer"
	(classification (ranking 'nlcd:NLCDNumeric)
		#{41 42 43 71 90 95} 'soilretentionEcology:ForestGrasslandWetland
		#{52 81}             'soilretentionEcology:ShrublandPasture
		#{21 22 23 24 31 82} 'soilretentionEcology:CropsBarrenDeveloped)
  (classification (ranking 'glc:GLCNumeric)
		#{1 2 3 4 5 6 7 8 9 15} 'soilretentionEcology:ForestGrasslandWetland
		#{10 11 12 13 14 17 18} 'soilretentionEcology:ShrublandPasture
    #{16 19 22}             'soilretentionEcology:CropsBarrenDeveloped)
  (classification (ranking 'mglulc:MGLULCNumeric)
    #{1 2 4 5 6 10 14}                         'soilretentionEcology:ForestWetland
    #{3 7 23}                                  'soilretentionEcology:DegradedForest
		#{8 9 20 21 22 24 25 26 28 29 30 31 32 33} 'soilretentionEcology:Savanna
    #{11 12 13 16 17}                          'soilretentionEcology:CroplandDeveloped)
  (classification (ranking 'domlulc:DOMLULCNumeric)
    #{1 2 4 6 8 9 11 18 35} 'soilretentionEcology:ForestAndShrubland
    #{22 24 62 63}          'soilretentionEcology:WaterWetlandsMangroves
	 	#{41 45 53}             'soilretentionEcology:ShadeCoffeeCocoa
    #{23 36 38 40 59}       'soilretentionEcology:IntensiveCroplandAndPasture
    #{42}                   'soilretentionEcology:UrbanAndRoads))

(defmodel percent-vegetation-cover 'soilretentionEcology:PercentVegetationCover
	(classification (ranking 'habitat:PercentCanopyCover)
		[80 100] 'soilretentionEcology:VeryHighVegetationCover
		[60 80]  'soilretentionEcology:HighVegetationCover
		[40 60]  'soilretentionEcology:ModerateVegetationCover
		[20 40]  'soilretentionEcology:LowVegetationCover
		[0 20]   'soilretentionEcology:VeryLowVegetationCover))

(defmodel successional-stage 'soilretentionEcology:SuccessionalStage
	 (classification (ranking 'ecology:SuccessionalStage)  
	 		#{5 6}      'soilretentionEcology:OldGrowth
	 		4           'soilretentionEcology:LateSuccession
	 		3           'soilretentionEcology:MidSuccession
      2           'soilretentionEcology:PoleSuccession
	 		1           'soilretentionEcology:EarlySuccession
	 		:otherwise  'soilretentionEcology:NoSuccession))

;;Sediment source value
;;GET WESTERN US SHAPEFILE INTO GEOSERVER/XML...
(defmodel sediment-source-value-annual 'soilretentionEcology:SedimentSourceValueAnnual
  [(ranking 'foobar:InWesternUs :as in-western-us?)]
 (classification (measurement 'soilretentionEcology:SedimentSourceValueAnnual "kg/ha")
      :when #(== 1 (:in-western-us? %)) 
  		0                          'soilretentionEcology:NoAnnualSedimentSource
  		[:exclusive 0 30000]       'soilretentionEcology:LowAnnualSedimentSource 
  		[30000 100000]             'soilretentionEcology:ModerateAnnualSedimentSource
  		[100000 :>]                'soilretentionEcology:HighAnnualSedimentSource)
	(classification (measurement 'soilretentionEcology:SedimentSourceValueAnnual "t/ha")
      0                     'soilretentionEcology:NoAnnualSedimentSource
  		[:exclusive 0 15]     'soilretentionEcology:LowAnnualSedimentSource 
  		[15 40]               'soilretentionEcology:ModerateAnnualSedimentSource
  		[40 :>]               'soilretentionEcology:HighAnnualSedimentSource))

;; source bayesian model for Puget Sound   	 
(defmodel source-puget 'soilretentionEcology:SedimentSourceValueAnnual
  (bayesian 'soilretentionEcology:SedimentSourceValueAnnual 
    :import   "aries.core::SedimentSourceValueAdHoc.xdsl"
    :keep     ('soilretentionEcology:SedimentSourceValueAnnual ) 
    :observed (sediment-source-value-annual) 
    :context  (soil-group slope soil-texture slope-stability  soil-erodibility precipitation-monthly  
              monthly-snowmelt monthly-temperature runoff vegetation-type percent-vegetation-cover 
              successional-stage)))

;; source bayesian model for Madagascar   	 
(defmodel source-mg 'soilretentionEcology:SedimentSourceValueAnnual
  (bayesian 'soilretentionEcology:SedimentSourceValueAnnual 
    :import   "aries.core::SedimentSourceValueMgAdHoc.xdsl"
    :keep     ('soilretentionEcology:SedimentSourceValueAnnual)
    :observed (sediment-source-value-annual) 
    :context  (soil-group slope soil-texture soil-erodibility precipitation-annual  
              storm-probability runoff vegetation-type percent-vegetation-cover))) 

;; source bayesian model for Dominican Republic
(defmodel source-dr 'soilretentionEcology:SedimentSourceValueAnnual
  (bayesian 'soilretentionEcology:SedimentSourceValueAnnual 
    :import   "aries.core::SedimentSourceValueDRAdHoc.xdsl"
    :keep     ('soilretentionEcology:SedimentSourceValueAnnual)
    :observed (sediment-source-value-annual) 
    :context  (soil-group slope soil-texture soil-erodibility precipitation-annual  
              storm-probability runoff vegetation-type percent-vegetation-cover))) 

;; Add deterministic model for USLE: Have data for it for the western U.S. and world in 1980.

;; ----------------------------------------------------------------------------------------------
;; use model
;; ----------------------------------------------------------------------------------------------

(defmodel floodplains 'soilretentionEcology:Floodplains
	(classification (ranking 'soilretentionEcology:Floodplains)
			0 'soilretentionEcology:NotInFloodplain
			1 'soilretentionEcology:InFloodplain))

(defmodel farmland 'soilretentionEcology:Farmland
	"Just a reclass of the regionally appropriate LULC layer"
	(classification (ranking 'nlcd:NLCDNumeric)
		82	       'soilretentionEcology:Farmland
		:otherwise 'soilretentionEcology:Farmland)
  (classification (ranking 'corine:CORINENumeric)
		[12 22 :inclusive]	 'soilretentionEcology:Farmland
		:otherwise           'soilretentionEcology:Farmland)
  (classification (ranking 'mglulc:MGLULCNumeric)
		#{11 12 13} 'soilretentionEcology:Farmland
		:otherwise 'soilretentionEcology:Farmland)
  (classification (ranking 'domlulc:DOMLULCNumeric)
		#{23 36 38 40 41 45 53 59}	'soilretentionEcology:Farmland
		:otherwise                'soilretentionEcology:Farmland)
	(classification (ranking 'glc:GLCNumeric)
		#{16 17 18} 'soilretentionEcology:Farmland
		:otherwise 'soilretentionEcology:Farmland))

;;Use normal dam storage (ac-ft in the U.S. or m^3 in the rest of the world) as a proxy for 
;;hyroelectric generation capacity - in reality dam heigh & flow are important factors but 
;;we don't have flow data.
(defmodel hydroelectric-use-level 'soilretentionEcology:HydroelectricUseLevel
	(classification (measurement 'soilretentionEcology:HydroelectricUseLevel "acre-feet")
    0                        'soilretentionEcology:NoHydroelectricUseLevel
		[:exclusive 0 500000]    'soilretentionEcology:ModerateHydroelectricUseLevel
		[500000 :>]              'soilretentionEcology:HighHydroelectricUseLevel)
	(classification (measurement 'soilretentionEcology:HydroelectricUseLevel "m^3")
    0                          'soilretentionEcology:NoHydroelectricUseLevel
		[:exclusive 0 15000000]    'soilretentionEcology:ModerateHydroelectricUseLevel
		[15000000 :>]              'soilretentionEcology:HighHydroelectricUseLevel))

;;Reservoirs layer for DR: presence/absence only.
(defmodel hydroelectric-use-presence 'soilretentionEcology:HydroelectricUsePresence
	(classification (ranking 'soilretentionEcology:HydroelectricUsePresence)
    0           'soilretentionEcology:HydroelectricUseAbsence
		1           'soilretentionEcology:HydroelectricUsePresent))

;; Models farmland in the floodplain, the non-Bayesian way (i.e., basic spatial overlap).
(defmodel farmers-deposition-use-puget 'soilretentionEcology:DepositionProneFarmers 
  (ranking 'soilretentionEcology:DepositionProneFarmers
       :state #(if (and (= (:floodplains %) 1.0)
                        (= (:farmlandpresent %) 82.0))
                    1
                    0)
      :context (
          (ranking 'lulc:NLCDNumeric :as farmlandpresent)
          (ranking 'soilretentionEcology:Floodplains :as floodplains)))) 

(defmodel farmers-deposition-use-mg 'soilretentionEcology:DepositionProneFarmers 
  (ranking 'soilretentionEcology:DepositionProneFarmers
       :state #(if (and (= (:floodplains %) 1.0)
                        (contains? #{11.0 12.0 13.0} (:farmlandpresent %)))
                    1
                    0)
      :context (
          (ranking 'mglulc:MGLULCNumeric :as farmlandpresent)
          (ranking 'soilretentionEcology:Floodplains :as floodplains)))) 

(defmodel farmers-deposition-use-dr 'soilretentionEcology:DepositionProneFarmers 
  (ranking 'soilretentionEcology:DepositionProneFarmers
       :state #(if (and (= (:floodplains %) 1.0)
                        (contains? #{23.0 36.0 38.0 40.0 41.0 45.0 53.0 59.0} (:farmlandpresent %)))
                    1
                    0)
      :context (
          (ranking 'domlulc:DOMLULCNumeric :as farmlandpresent)
          (ranking 'soilretentionEcology:Floodplains :as floodplains)))) 

;; Models farmland in regions with erodible soils, the non-Bayesian way (i.e., basic spatial overlap).
;; Gary: does this look right?
;;(defmodel farmers-erosion-use-puget 'soilretentionEcology:ErosionProneFarmers 
  ;;[(ranking 'nlcd:NLCDNumeric :as farmlandpresent)
   ;;(ranking 'soilretentionEcology:SedimentSourceValue :as sediment-source-value-puget)]
   ;;(ranking 'soilretentionEcology:ErosionProneFarmers
     ;;  :state #(if (and (= (:floodplains %) 1.0)  ;;WHAT should this state be?
       ;;                 (= (:farmlandpresent %) 82.0))
       ;;             1
       ;;             0))) 

;;Still need defmodels for all components of fisheries BNs.  What about deterministic nodes?
;; Need an undiscretization defmodel before this, for the "observed"? In the long run, could take 2 paths:
;; 1) ditch fisheries BNs & use source/use models for actual fisheries
;; 2) use BNs as generalized fisheries impact model.
;;(defmodel fishermen-use-puget 'soilretentionEcology:FishermenUse 
	  ;;(bayesian 'soilretentionEcology:FishermenUse  
	 ;; 	:import   "aries.core::SedimentFishermenUse.xdsl"
	 ;; 	:keep     ('soilretentionEcology:FishermenUse)
	 ;;	 	:context  (lakes rivers coastline coastal-wetlands salmon-spawning-grounds public-access population-density)))

;;defmodel fishermen-use-mg 'soilretentionEcology:FishermenUse 
	;;  (bayesian 'soilretentionEcology:FishermenUse  
	 ;; 	:import   "aries.core::SedimentFishermenUseMg.xdsl"
	 ;; 	:keep     ('soilretentionEcology:FishermenUse)
	 ;;	 	:context  (lakes rivers coastline coastal-wetlands mangroves reefs seagrass population-density)))

;; ----------------------------------------------------------------------------------------------
;; sink model
;; ----------------------------------------------------------------------------------------------

;; Make reservoir storage a new xml entity, with presence/absence, rename "reservoir storage" in the flood model, BNs, ontologies, xml...
;; Gary says OK to use rasterized point file for dams/reservoirs
(defmodel reservoirs 'soilretentionEcology:Reservoirs 
  (classification (ranking 'soilretentionEcology:Reservoirs)
		  0          'soilretentionEcology:ReservoirAbsent
		  :otherwise 'soilretentionEcology:ReservoirPresent))

(defmodel stream-gradient 'soilretentionEcology:StreamGradient 
  (classification (ranking 'soilretentionEcology:StreamGradient)
    [:<   1.15]  'soilretentionEcology:LowStreamGradient
    [1.15 2.86]  'soilretentionEcology:ModerateStreamGradient
    [2.86 :>]    'soilretentionEcology:HighStreamGradient))

(defmodel floodplain-vegetation-cover 'soilretentionEcology:FloodplainVegetationCover 
  (classification (ranking 'soilretentionEcology:FloodplainVegetationCover)
    [0 20]   'soilretentionEcology:VeryLowFloodplainVegetationCover
    [20 40]  'soilretentionEcology:LowFloodplainVegetationCover
    [40 60]  'soilretentionEcology:ModerateVegetationCover
    [60 80]  'soilretentionEcology:HighFloodplainVegetationCover
    [80 100] 'soilretentionEcology:VeryHighFloodplainVegetationCover))

;; Need to add floodplain width to geoserver & xml.  These units are in decimal degrees - these 
;; correspond roughly tp breakpoints of 375, 820, and 1320 m.
(defmodel floodplain-width 'soilretentionEcology:FloodplainWidth 
  (classification (measurement 'soilretentionEcology:FloodplainWidth "m")
    [0 350]     'soilretentionEcology:VeryNarrowFloodplain
    [350 800]   'soilretentionEcology:NarrowFloodplain
    [800 1300]  'soilretentionEcology:WideFloodplain
    [1300 :>]   'soilretentionEcology:VeryWideFloodplain))

(defmodel levees 'soilretentionEcology:Levees 
  (classification (ranking 'soilretentionEcology:Levees)
		  0          'soilretentionEcology:LeveeAbsent
		  :otherwise 'soilretentionEcology:LeveePresent))

;;These are arbitrary numbers discretized based on the "low" soil erosion level defined by the US & global datasets, respectively.
;; Have these numbers reviewed by someone knowledgable about sedimentation.
(defmodel sediment-sink-annual 'soilretentionEcology:AnnualSedimentSink 
  [(ranking 'foobar:InWesternUs :as in-western-us?)]
  (classification (measurement 'soilretentionEcology:AnnualSedimentSink "kg/ha")
      :when #(== 1 (:in-western-us? %)) 
       [20000 30000]          'soilretentionEcology:HighAnnualSedimentSink
       [10000 20000]          'soilretentionEcology:ModerateAnnualSedimentSink
       [:exclusive 0 10000]   'soilretentionEcology:LowAnnualSedimentSink
       0                      'soilretentionEcology:NoAnnualSedimentSink) 
  (classification (measurement 'soilretentionEcology:AnnualSedimentSink "t/ha")
       [10 15]              'soilretentionEcology:HighAnnualSedimentSink
       [5 10]               'soilretentionEcology:ModerateAnnualSedimentSink
       [:exclusive 0 5]     'soilretentionEcology:LowAnnualSedimentSink
       0                    'soilretentionEcology:NoAnnualSedimentSink)) 

;;If we successfully get FPWidth data for Mg & DR, add these to the "context" part of the model.
(defmodel sediment-sink-mg 'soilretentionEcology:AnnualSedimentSink
  (bayesian 'soilretentionEcology:AnnualSedimentSink 
    :import  "aries.core::SedimentSinkMg.xdsl"
    :keep    ('soilretentionEcology:AnnualSedimentSink)
    :observed (sediment-sink-annual) 
    :context (reservoirs stream-gradient floodplain-vegetation-cover)))

(defmodel sediment-sink-us 'soilretentionEcology:AnnualSedimentSink
  (bayesian 'soilretentionEcology:AnnualSedimentSink    
    :import  "aries.core::SedimentSink.xdsl"
    :keep    ('soilretentionEcology:AnnualSedimentSink)
    :observed (sediment-sink-annual) 
    :context (reservoirs stream-gradient floodplain-vegetation-cover floodplain-width levees)))

(defmodel sediment-sink-dr 'soilretentionEcology:AnnualSedimentSink
  (bayesian 'soilretentionEcology:AnnualSedimentSink 
    :import  "aries.core::SedimentSinkDR.xdsl"
    :keep    ('soilretentionEcology:AnnualSedimentSink)
    :observed (sediment-sink-annual) 
    :context (reservoirs stream-gradient floodplain-vegetation-cover)))

;; ----------------------------------------------------------------------------------------------
;; dependencies for the flow model
;; ----------------------------------------------------------------------------------------------

;;Everything below needs to be updated correctly for sediment.
 	 								
;;(defmodel altitude 'geophysics:Altitude
  ;;(measurement 'geophysics:Altitude "m"))	 								
 
;; ---------------------------------------------------------------------------------------------------	 	 	
;; overall models 
;; ---------------------------------------------------------------------------------------------------	 	 	

;; all data, for testing and storage
 ;;(defmodel data 'aestheticService:AestheticEnjoyment 
	;;(identification 'aestheticService:AestheticEnjoyment)
		;;  :context (
		;;	source :as source
		;;	homeowners :as use
		;;	sink :as sink
		;;	altitude :as altitude))
			
;; the real enchilada
;;(defmodel view 'aestheticService:AestheticView
 ;; (span 'aestheticService:LineOfSight 
  	;;    'aestheticService:TheoreticalNaturalBeauty
  	;;    'aestheticService:HomeownerViewUse
    ;;  	'aestheticService:TotalVisualBlight
    ;;  	'aestheticService:View
  	;;    'geophysics:Altitude
  ;; 	:sink-type        :relative
  ;; 	:use-type         :relative
  ;; 	:benefit-type     :non-rival
  ;; 	:downscaling-factor 3
  ;; 	:rv-max-states    10 
  ;;  :context
    ;;     (source homeowners sink altitude
    ;;     (ranking 'eserv:SourceThreshold :value 50)
    ;;     (ranking 'eserv:SinkThreshold :value 0.3)
    ;;     (ranking 'eserv:UseThreshold :value 0.1)
    ;;     (ranking 'eserv:TransitionThreshold :value 1.0))
;;))
