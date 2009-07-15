
;; ---------------------------------------------------------------------------------------------------
;; test runs; should eventually contain functions to test run all ARIES models at the command line
;; ---------------------------------------------------------------------------------------------------

;; run the view model
(defn save-view-model
	"Run the view model at the given resolution" 
	[resolution output-file]
	(let [view-params               {:decay-rate       0.95
					 :trans-threshold  1.0
					 :source-threshold 0.05
					 :sink-threshold   0.20
					 :use-threshold    0.05
				 	 :source-threshold 0.05
	 				 :sink-threshold   0.20
	         :use-threshold    0.05
				 	 :sink-type       :relative
				 	 :use-type        :relative
				 	 :benefit-type    :non-rival}]
				(aries.demo/save-gssm-demo-data 
					'aestheticService:SensoryEnjoyment
					'aestheticService:AestheticViewshedUse
					'aestheticService:ViewSink
					'aestheticService:LineOfSight view-params resolution output-file)))
		
(defn save-proximity-model
  "Run the proximity model at the given resolution"
  [resolution output-file]
  (let [view-params
	{:decay-rate      0.6
	 :trans-threshold 1.0
	 :source-threshold 0.0
	 :sink-threshold   0.0
	 :use-threshold    0.0
	 :sink-type       :relative
	 :use-type        :relative
	 :benefit-type    :non-rival}]
    (aries.demo/save-gssm-demo-data
     'aestheticService:ProximityToBeauty
     'aestheticService:AestheticProximityUse
     'aestheticService:ProximitySink
     'aestheticService:Proximity view-params resolution output-file)))

(save-view-model 256 "/home/gjohnson/viewflow.nc")
;(save-proximity-model 256 "C:/A/results/proximityflow.nc")