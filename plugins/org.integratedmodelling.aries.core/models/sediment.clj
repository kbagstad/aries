(ns aries.sediment
  (:refer modelling :only (defmodel measurement classification categorization ranking identification bayesian))
  (:refer aries :only (span)))

;; ----------------------------------------------------------------------------------------------
;; provision model
;; ----------------------------------------------------------------------------------------------

(defmodel soil-group 'floodService:HydrologicSoilsGroup
	"Relevant soil group"
	(classification (categorization 'floodService:HydrologicSoilsGroup)
			"A"        'floodService:SoilGroupA
			"B"        'floodService:SoilGroupB
			"C"        'floodService:SoilGroupC
			"D"        'floodService:SoilGroupD))

(defmodel slope 'floodService:Slope
		(classification (ranking 'geophysics:DegreeSlope)
			 :units       "degrees" 
			 [:< 1.15] 	  'floodService:Level
			 [1.15 4.57] 	'floodService:GentlyUndulating
			 [4.57 16.70] 'floodService:RollingToHilly
			 [16.70 :>] 	'floodService:SteeplyDissectedToMountainous))

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

;;This discretization for Puget Sound.  This is the soil erodibility factor from USLE (unitless).
(defmodel soil-erodibility-puget 'soilretentionEcology:SoilErodibility
     (classification (ranking 'soilretentionEcology:SoilErodibility)
       [:< 0.15]    'soilretentionEcology:VeryLowSoilErodibility
       [0.15 0.2]   'soilretentionEcology:LowSoilErodibility
       [0.2 0.25]   'soilretentionEcology:ModerateSoilErodibility
       [0.25 0.3]   'soilretentionEcology:HighSoilErodibility
       [0.3 :>]     'soilretentionEcology:VeryHighSoilErodibility))

;;This discretization for DR & Mg, based on global data.  This is the soil erodibility factor from USLE (unitless).
(defmodel soil-erodibility-global 'soilretentionEcology:SoilErodibility
     (classification (ranking 'soilretentionEcology:SoilErodibility)
      [:< 0.02]       'soilretentionEcology:VeryLowSoilErodibility
      [0.02 0.275]    'soilretentionEcology:LowSoilErodibility
      [0.275 0.325]   'soilretentionEcology:ModerateSoilErodibility
      [0.325 0.375]   'soilretentionEcology:HighSoilErodibility
      [0.375 :>]      'soilretentionEcology:VeryHighSoilErodibility))

;;Monthly precipitation for Puget Sound.
(defmodel precipitation-puget 'floodService:Precipitation
	"FIXME this is total monthly precipitation."
	(classification (measurement 'habitat:Precipitation "in")
		[:< 3] 	  'floodService:VeryLowPrecipitation
		[3 6] 	  'floodService:LowPrecipitation
		[6 12] 	  'floodService:ModeratePrecipitation
		[12 24] 	'floodService:HighPrecipitation
		[24 :>] 	'floodService:VeryHighPrecipitation))

;;Annual precipitation for Mg & DR
(defmodel precipitation-global 'floodService:Precipitation
	"FIXME this is annual precipitation."
	(classification (measurement 'habitat:Precipitation "mm")
    [:< 600] 	    'floodService:VeryLowPrecipitation
		[600 1200] 	  'floodService:LowPrecipitation
		[1200 1800]   'floodService:ModeratePrecipitation
		[1800 2200] 	'floodService:HighPrecipitation
		[2200 :>] 	  'floodService:VeryHighPrecipitation))

;; Surface temperature - again, should be monthly and matched by temporal extents.  For Puget Sound only.
(defmodel monthly-temperature 'floodService:MonthlyTemperature
		(classification (ranking 'geophysics:GroundSurfaceTemperature "C")
			 [4 :>] 	'floodService:HighTemperature
			 [-4 4] 	'floodService:ModerateTemperature
			 [:< -4] 	'floodService:LowTemperature))

;;Snowmelt, use only in Puget Sound
(defmodel monthly-snowmelt 'soilretentionEcology:MonthlySnowmelt
	(classification (measurement 'soilretentionEcology:MonthlySnowmelt "mm/month")
		0 	                  'soilretentionEcology:NoSnowmelt
		[:exclusive 0 50] 	  'soilretentionEcology:LowSnowmelt
		[50 100] 	            'soilretentionEcology:ModerateSnowmelt
		[100 :>]            	'soilretentionEcology:HighSnowmelt))

;;Tropical storm probability, use only in DR & Mg
(defmodel storm-probability 'coastalProtection:TropicalStormProbability
	(ranking 'habitat:TropicalStormProbability
      0     'habitat:NoTropicalStormProbability
      [1 5]   'habitat:ModerateTropicalStormProbability
      [6 10]  'habitat:HighTropicalStormProbability))

;;Annual runoff, whereas snowmelt, precipitation, and temperature are monnthly, so this is problematic.
;;Could divide yearly runoff by 12 but obviously it's not evenly distributed throughout the year.
(defmodel runoff 'soilretentionEcology:Runoff
	(classification (measurement 'soilretentionEcology:Runoff "mm/yr")
		[0 200] 	    'soilretentionEcology:VeryLowRunoff
		[200 600] 	  'soilretentionEcology:LowRunoff
		[600 1200]  	'soilretentionEcology:ModerateRunoff
		[1200 2400] 	'soilretentionEcology:HighRunoff
		[2400 :>] 	  'soilretentionEcology:VeryHighRunoff))

;;Vegetation type, Puget Sound
(defmodel vegetation-type-puget 'soilretentionEcology:VegetationType
	"Just a reclass of the NLCD land use layer"
	(classification (ranking 'nlcd:NLCDNumeric)
		#{41 42 43 71 90 95} 'soilretentionEcology:ForestGrasslandWetland
		#{52 81}             'soilretentionEcology:ShrublandPasture
		#{21 22 23 24 31 82} 'soilretentionEcology:CropsBarrenDeveloped))

;;Vegetation type, global.  These concepts don't yet exist in the glc2000 ontology.
(defmodel vegetation-type-global 'soilretentionEcology:VegetationType
  (classification (ranking 'glc2000:GLCNumeric)
		#{1 2 3 4 5 6 7 8 9 15} 'soilretentionEcology:ForestGrasslandWetland
		#{10 11 12 13 14 17 18} 'soilretentionEcology:ShrublandPasture
    #{16 19 22}             'soilretentionEcology:CropsBarrenDeveloped)) 

;;These concepts don't yet exist in the mglulc ontology.
;;Vegetation type, Mg
(defmodel vegetation-type-mg 'soilretentionEcology:VegetationType
  (classification (ranking 'mglulc:MgLULCNumeric)
    #{1 2 4 5 6 10 14} 'soilretentionEcology:ForestWetland
    #{3 7}             'soilretentionEcology:DegradedForest
		#{8 9}             'soilretentionEcology:Savanna
    #{16 17}           'soilretentionEcology:CroplandDeveloped)) 

;;NEED TO GET CORRECT NUMBERS FROM LEE & NATHALY...
;;Vegetation type, DR.  These concepts don't yet exist in the drlulc ontology.
(defmodel vegetation-type-dr 'soilretentionEcology:VegetationType
  (classification (ranking 'drlulc:DRLULCNumeric)
    #{1 2 4 6 8 9} 'soilretentionEcology:ForestAndShrubland
    #{62 63}                     'soilretentionEcology:WaterWetlandsMangroves
		#{} 'soilretentionEcology:ShadeCoffeeCocoa
    #{}             'soilretentionEcology:IntensiveCroplandAndPasture
    #{}             'soilretentionEcology:UrbanAndRoads))

(defmodel percent-vegetation-cover 'floodService:PercentVegetationCover
	(classification (ranking 'habitat:PercentCanopyCover)
		[80 100] 'floodService:VeryHighVegetationCover
		[60 80] 'floodService:HighVegetationCover
		[40 60] 'floodService:ModerateVegetationCover
		[20 40] 'floodService:LowVegetationCover
		[0 20]  'floodService:VeryLowVegetationCover))

(defmodel successional-stage 'floodService:SuccessionalStage
	 (classification (ranking 'ecology:SuccessionalStage)  
	 		#{5 6}      'floodService:OldGrowth
	 		4           'floodService:LateSuccession
	 		3           'floodService:MidSuccession
	 		2           'floodService:EarlySuccession
	 		1           'floodService:PoleSuccession
	 		:otherwise  'floodService:NoSuccession))

;;Discretization below for Puget Sound.
(defmodel sediment-source-value-puget 'soilretentionEcology:SedimentSourceValue
	(classification (measurement 'soilretentionEcology:SedimentSourceValue "kg/ha-yr")
  		0                          'soilretentionEcology:NoSedimentSource
  		[:exclusive 0 30000]       'soilretentionEcology:LowSedimentSource 
  		[30000 100000]             'soilretentionEcology:ModerateSedimentSource
  		[100000 :>]                'soilretentionEcology:HighSedimentSource))

;;Discretization below for DR & Mg.  Data are from 1980, so not temporally consistent.
(defmodel sediment-source-value-global 'soilretentionEcology:SedimentSourceValue
	(classification (measurement 'soilretentionEcology:SedimentSourceValue "tons/ha-yr")
      0                     'soilretentionEcology:NoSedimentSource
  		[:exclusive 0 15]     'soilretentionEcology:LowSedimentSource 
  		[15 40]               'soilretentionEcology:ModerateSedimentSource
  		[40 :>]               'soilretentionEcology:HighSedimentSource))

;; source bayesian model for Puget Sound   	 
(defmodel source-puget 'soilretentionEcology:SedimentSourceValue
  (bayesian 'soilretentionEcology:SedimentSourceValue 
    :import   "aries.core::SedimentSourceValueAdHoc.xdsl"
    :keep     ('soilretentionEcology:SedimentSourceValue
    :observed (sediment-source-value-puget) 
    :context  (soil-group slope soil-texture slope-stability  soil-erodibility-puget precipitation-puget  
              monthly-snowmelt monthly-temperature runoff vegetation-type-puget percent-vegetation-cover 
              successional-stage)))

;; source bayesian model for Madagascar   	 
(defmodel source-mg 'soilretentionEcology:SedimentSourceValue
  (bayesian 'soilretentionEcology:SedimentSourceValue 
    :import   "aries.core::SedimentSourceValueMgAdHoc.xdsl"
    :keep     ('soilretentionEcology:SedimentSourceValue
    :observed (sediment-source-value-global) 
    :context  (soil-group slope soil-texture soil-erodibility-global precipitation-global  
              storm-probability runoff vegetation-type-mg percent-vegetation-cover)))

;; source bayesian model for Dominican Republic
;; make sure to properly rename the .xdsl file   	 
(defmodel source-dr 'soilretentionEcology:SedimentSourceValue
  (bayesian 'soilretentionEcology:SedimentSourceValue 
    :import   "aries.core::SedimentSourceValueDRAdHoc.xdsl"
    :keep     ('soilretentionEcology:SedimentSourceValue
    :observed (sediment-source-value-global) 
    :context  (soil-group slope soil-texture soil-erodibility-global precipitation-global  
              storm-probability runoff vegetation-type-dr percent-vegetation-cover)))

;; Add deterministic model for USLE: Have data for it for the western U.S. and world in 1980.

;; ----------------------------------------------------------------------------------------------
;; use model
;; ----------------------------------------------------------------------------------------------

;;Still need defmodels for all components of fisheries BNs.  In the long run, could take 2 paths:
;; 1) ditch fisheries BNs & use source/use models for actual fisheries
;; 2) use BNs as generalized fisheries impact model.

(defmodel floodplains 'floodService:Floodplain
	(classification (ranking 'floodService:Floodplains)
			0 'floodService:NotInFloodplain
			1 'floodService:InFloodplain))

;; Not sure what the "agent" statement below is saying.  Still need defmodels for farmland in Mg & DR.
(defmodel farmland 'floodService:Farmland
	"Just a reclass of the NLCD land use layer"
	(classification (ranking 'nlcd:NLCDNumeric)
		82	       'floodService:FarmlandPresent
		:otherwise 'floodService:FarmlandAbsent
;    :agent     "aries/flood/farm"
    :editable  true))

;;Use normal dam storage (ac-ft) as a proxy for hyroelectric generation capacity -
;;in reality dam heigh & flow are important factors but we don't have flow data.
;; NEED TO do wfs2opal for reservoirs, use "normal_sto" as the attribute of interest.
(defmodel hydroelectric-use-level 'soilretentionEcology:HydroelectricUseLevel
	(classification (ranking 'soilretentionEcology:HydroelectricUseLevel)
    0                        'soilretentionEcology:NoHydroelectricUse
		[:exclusive 0 500000]    'soilretentionEcology:ModerateHydroelectricUse
		[500000 :>]              'soilretentionEcology:HighHydroelectricUse))

;;Have rasterized the dam point layer for Mg but it's not yet in Geoserver.

;;Reservoirs layer for DR: presence/absence only.
(defmodel hydroelectric-use-presence 'soilretentionEcology:HydroelectricUsePresence
	(classification (ranking 'soilretentionEcology:HydroelectricUse)
    0           'soilretentionEcology:HydroelectricUseAbsence
		1           'soilretentionEcology:HydroelectricUsePresent))

;; Gary: do we need an undiscretization defmodel before this, for the "observed"?
;; Farmer users in floodplains, i.e., where sedimentation is desirable or undesirable, depending on conditions
(defmodel farmers-use 'soilretentionEcology:DepositionProneFarmers 
	  (bayesian 'soilretentionEcology:DepositionProneFarmers 
	  	:import   "aries.core::SedimentDepositionProneFarmersUse.xdsl"
	  	:keep     ('soilretentionEcology:DepositionProneFarmers)
	 	 	:context  (farmland floodplains)))

;;Get Gary's input on what to do with "erosion prone farmers" since erosion source value goes into the BN.

;; Fishermen use model: lots of work still to do on this one.  What about deterministic nodes?
;; Need an undiscretization defmodel before this, for the "observed"?
(defmodel fishermen-use-puget 'soilretentionEcology:FishermenUse 
	  (bayesian 'soilretentionEcology:FishermenUse  
	  	:import   "aries.core::SedimentFishermenUse.xdsl"
	  	:keep     ('soilretentionEcology:FishermenUse)
	 	 	:context  (lakes rivers coastline coastal-wetlands salmon-spawning-grounds public-access population-density)))

defmodel fishermen-use-mg 'soilretentionEcology:FishermenUse 
	  (bayesian 'soilretentionEcology:FishermenUse  
	  	:import   "aries.core::SedimentFishermenUseMg.xdsl"
	  	:keep     ('soilretentionEcology:FishermenUse)
	 	 	:context  (lakes rivers coastline coastal-wetlands mangroves reefs seagrass population-density)))

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
  (classification (measurement 'soilretentionEcology:StreamGradient "�")
    [:<   1.15]  'soilretentionEcology:LowStreamGradient
    [1.15 2.86] 'soilretentionEcology:ModerateStreamGradient
    [2.86 :>]   'soilretentionEcology:HighStreamGradient))

(defmodel floodplain-vegetation-cover 'soilretentionEcology:FloodplainVegetationCover 
  (classification (measurement 'soilretentionEcology:FloodplainVegetationCover "%")
    [0 20]  'soilretentionEcology:VeryLowFloodplainVegetationCover
    [20 40]  'soilretentionEcology:LowFloodplainVegetationCover
    [40 60] 'soilretentionEcology:ModerateVegetationCover
    [60 80]  'soilretentionEcology:HighFloodplainVegetationCover
    [80 100]   'soilretentionEcology:VeryHighFloodplainVegetationCover))

;; Need to add floodplain width to geoserver & xml.  These units are in decimal degrees - these 
;; correspond roughly tp breakpoints of 375, 820, and 1320 m.
(defmodel floodplain-width 'soilretentionEcology:FloodplainWidth 
  (classification (measurement 'soilretentionEcology:FloodplainWidth "decimal degrees")
    [0 0.0034]  'soilretentionEcology:VeryNarrowFloodplain
    [0.0034 0.0074]  'soilretentionEcology:NarrowFloodplain
    [0.0074 0.0119] 'soilretentionEcology:WideFloodplain
    [0.0119 :>]   'soilretentionEcology:VeryWideFloodplain))

(defmodel levees 'soilretentionEcology:Levees 
  (classification (ranking 'soilretentionEcology:Levees)
		  0          'soilretentionEcology:LeveeAbsent
		  :otherwise 'soilretentionEcology:LeveePresent))

;;These are arbitrary numbers discretized based on the "low" soil erosion level defined by the global dataset.
;; Have these numbers reviewed by someone knowledgable about sedimentation.
(defmodel sediment-sink-global 'soilretentionEcology:SedimentSink
  "tons/ha-yr"  
  (classification  'soilretentionEcology:SedimentSink
       [10 15]              'soilretentionEcology:HighSedimentSink
       [5 10]               'soilretentionEcology:ModerateSedimentSink
       [:exclusive 0 5]     'soilretentionEcology:LowSedimentSink
       0                    'soilretentionEcology:NoSedimentSink)) 

;;These are arbitrary numbers discretized based on the "low" soil erosion level defined by the western U.S. dataset.
;; Have these numbers reviewed by someone knowledgable about sedimentation.
(defmodel sediment-sink-western-US 'soilretentionEcology:SedimentSink
  "kg/ha-yr"
  (classification 'soilretentionEcology:SedimentSink)
       [20000 30000]          'soilretentionEcology:HighSedimentSink
       [10000 20000]          'soilretentionEcology:ModerateSedimentSink
       [:exclusive 0 10000]   'soilretentionEcology:LowSedimentSink
       0                      'soilretentionEcology:NoSedimentSink)) 

;;If we successfully get FPWidth data for Mg & DR, add these to the "context" part of the model.
(defmodel sink-mg 'soilretentionEcology:SedimentSink
  (bayesian 'soilretentionEcology:SedimentSink 
    :import  "aries.core::SedimentSinkMg.xdsl"
    :keep    ('soilretentionEcology:SedimentSink)
    :observed (sediment-sink-global) 
    :context (reservoirs stream-gradient floodplain-vegetation-cover)))

(defmodel sink-us 'soilretentionEcology:SedimentSink
  (bayesian 'soilretentionEcology:SedimentSink    
    :import  "aries.core::SedimentSink.xdsl"
    :keep    ('soilretentionEcology:SedimentSink)
    :observed (sediment-sink-western-US) 
    :context (reservoirs stream-gradient floodplain-vegetation-cover floodplain-width levees)))

(defmodel sink-dr 'soilretentionEcology:SedimentSink
  (bayesian 'soilretentionEcology:SedimentSink 
    :import  "aries.core::SedimentSinkDR.xdsl"
    :keep    ('soilretentionEcology:SedimentSink)
    :observed (sediment-sink-global) 
    :context (reservoirs stream-gradient floodplain-vegetation-cover)))

;; ----------------------------------------------------------------------------------------------
;; dependencies for the flow model
;; ----------------------------------------------------------------------------------------------
 	 								
(defmodel altitude 'geophysics:Altitude
  (measurement 'geophysics:Altitude "m"))	 								
 
;; ---------------------------------------------------------------------------------------------------	 	 	
;; overall models 
;; ---------------------------------------------------------------------------------------------------	 	 	

;; all data, for testing and storage
(defmodel data 'aestheticService:AestheticEnjoyment 
	(identification 'aestheticService:AestheticEnjoyment)
		:context (
			source :as source
			homeowners :as use
			sink :as sink
			altitude :as altitude))
			
;; the real enchilada
(defmodel view 'aestheticService:AestheticView
  (span 'aestheticService:LineOfSight 
  	    'aestheticService:TheoreticalNaturalBeauty
  	    'aestheticService:HomeownerViewUse
      	'aestheticService:TotalVisualBlight
      	'aestheticService:View
  	    'geophysics:Altitude
   	:sink-type        :relative
   	:use-type         :relative
   	:benefit-type     :non-rival
   	:downscaling-factor 3
   	:rv-max-states    10 
    :context
         (source homeowners sink altitude
          (ranking 'eserv:SourceThreshold :value 50)
          (ranking 'eserv:SinkThreshold :value 0.3)
          (ranking 'eserv:UseThreshold :value 0.1)
          (ranking 'eserv:TransitionThreshold :value 1.0))
))