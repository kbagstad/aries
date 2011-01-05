(ns core.contexts.test
  (:refer-clojure :rename {count length})
  (:refer modelling :only [defcontext model])
  (:refer geospace :only [grid]))

(defcontext chehalis-clearcut
  "Chehalis watershed @ 256 linear with an ugly hole in the forest as a scenario test. "
  (grid 
    256 
    "EPSG:4326 POLYGON ((-124.27 46.763, -124.27 47.55, -122.42 47.55, -122.42 46.763, -124.27 46.763))"))

(defcontext fishfucker
  "Breaks fisheries model."
  (grid 
    256 
    "EPSG:4326 POLYGON ((49.50439452435933 -17.759149753007183, 49.493408196235784 -17.759149753007183, 49.449462883741646 -17.727758607463077, 49.37255858687688 -17.675427815957313, 49.35058593063069 -17.66495982813849, 49.24072264939533 -17.591666876009576, 49.196777336902066 -17.570720565669323, 49.020996086926374 -17.476432194838182, 48.95507811818515 -17.434510549170593, 48.88916014944482 -17.40306299098127, 48.746337883839736 -17.308687884433727, 48.69140624322205 -17.277218733449075, 48.57055663386404 -17.17228277937408, 48.52661132136989 -17.13029174122068, 48.48266600887573 -17.0987922353694, 48.372802727641265 -17.004261783992508, 48.328857415148 -16.951724232143786, 48.27392577453032 -16.90968361327309, 48.14208983704877 -16.772986928696728, 48.12011718080171 -16.751947920914766, 48.054199212060475 -16.657243643107517, 48.04321288393694 -16.62566512571208, 47.96630858707307 -16.50983282467138, 47.955322258949536 -16.467694746059603, 47.93334960270246 -16.446622269419848, 47.92236327457893 -16.372850599578918, 47.91137694645539 -16.33068281816964, 47.900390618331855 -16.299051012373862, 47.87841796208567 -16.16192095159496, 47.87841796208567 -16.098598008120117, 47.86743163396214 -16.066928955272203, 47.86743163396214 -16.035254860176362, 47.86743163396214 -15.982453520806773, 47.86743163396214 -15.919073515823465, 47.86743163396214 -15.845104900124415, 47.86743163396214 -15.813395758315428, 47.86743163396214 -15.67593214920715, 47.86743163396214 -15.644196598742518, 47.87841796208567 -15.61245612502941, 47.88940429020831 -15.50661910452779, 47.88940429020831 -15.474857400585364, 47.99926757144369 -15.061514889023897, 48.0322265558143 -14.987239523735955, 48.054199212060475 -14.97662664958677, 48.054199212060475 -14.944784873055324, 48.08715819643109 -14.891704752189005, 48.12011718080171 -14.838611551365778, 48.164062493295845 -14.796127601613264, 48.17504882141939 -14.764259176581756, 48.208007805789094 -14.753635329531923, 48.27392577453032 -14.679253893206292, 48.31787108702448 -14.62610879688501, 48.350830071394185 -14.604847153064716, 48.471679680753105 -14.519780044348327, 48.493652336999276 -14.498508147471092, 48.51562499324635 -14.487871432957759, 48.54858397761696 -14.445319475722721, 48.57055663386404 -14.434680213330095, 48.62548827448172 -14.3921180817007, 48.65844725885143 -14.3921180817007, 48.68041991509851 -14.381476279991917, 48.76831054008681 -14.328259675789692, 48.77929686820945 -14.328259675789692, 48.81225585258006 -14.306969495875368, 48.823242180703595 -14.306969495875368, 48.85620116507422 -14.306969495875368, 48.86718749319775 -14.306969495875368, 48.87817382132128 -14.29632364909904, 48.88916014944482 -14.29632364909904, 48.93310546193807 -14.285677298234793, 48.94409179006161 -14.285677298234793, 48.966064446308685 -14.285677298234793, 48.988037102555765 -14.285677298234793, 49.03198241504992 -14.285677298234793, 49.05395507129699 -14.285677298234793, 49.119873040037305 -14.285677298234793, 49.15283202440792 -14.285677298234793, 49.174804680655 -14.285677298234793, 49.262695305642396 -14.29632364909904, 49.284667961889475 -14.29632364909904, 49.29565429001301 -14.29632364909904, 49.38354491500041 -14.306969495875368, 49.405517571247486 -14.317614838220102, 49.42749022749456 -14.328259675789692, 49.5263671806055 -14.34954783522964, 49.54833983685258 -14.34954783522964, 49.60327147747026 -14.34954783522964, 49.69116210245766 -14.37083397144844, 49.757080071198885 -14.381476279991917, 49.822998039939215 -14.3921180817007, 49.86694335243336 -14.40275937623182, 49.88891600868044 -14.40275937623182, 49.932861321174585 -14.413400163242416, 49.95483397742166 -14.413400163242416, 50.02075194616199 -14.434680213330095, 50.086669914903204 -14.445319475722721, 50.13061522739646 -14.445319475722721, 50.174560539890614 -14.455958229224205, 50.2294921805083 -14.466596473492093, 50.306396477372154 -14.477234208184042, 50.350341789866306 -14.487871432957759, 50.42724608673018 -14.487871432957759, 50.438232414853715 -14.487871432957759, 50.47119139922432 -14.498508147471092, 50.51513671171847 -14.509144351381957, 50.592041008582335 -14.53041522602828, 50.65795897732356 -14.55168405416172, 50.70190428981682 -14.55168405416172, 50.74584960231096 -14.562317699931805, 50.80078124292864 -14.572950833047798, 50.833740227299266 -14.572950833047798, 50.855712883545436 -14.594215559956037, 50.87768553979251 -14.594215559956037, 50.9326171804102 -14.62610879688501, 50.943603508533734 -14.62610879688501, 50.99853514915052 -14.647368381902158, 51.03149413352114 -14.657997401507203, 51.03149413352114 -14.668625905388819, 51.04248046164467 -14.668625905388819, 51.07543944601528 -14.700508319286508, 51.086425774138824 -14.711134756868285, 51.10839843038589 -14.732386079412581, 51.10839843038589 -14.753635329531923, 51.11938475850943 -14.764259176581756, 51.13037108663296 -14.796127601613264, 51.15234374287915 -14.817370618138845, 51.16333007100269 -14.827991345334363, 51.17431639912622 -14.859850398578509, 51.185302727249756 -14.881087157065497, 51.1962890553733 -14.944784873055324, 51.20727538349684 -14.966013249531496, 51.21826171162037 -15.029685754511277, 51.21826171162037 -15.050905705677755, 51.229248039743915 -15.114552869889224, 51.229248039743915 -15.146369297579165, 51.229248039743915 -15.19938604649378, 51.24023436786744 -15.241789853890277, 51.24023436786744 -15.337167129474626, 51.24023436786744 -15.368949894446736, 51.24023436786744 -15.432500879789382, 51.24023436786744 -15.485445177375434, 51.25122069599098 -15.559544419345807, 51.25122069599098 -15.591293078516173, 51.25122069599098 -15.644196598742518, 51.25122069599098 -15.665354179967126, 51.25122069599098 -15.728813768399178, 51.25122069599098 -15.739388444513061, 51.25122069599098 -15.802824939269316, 51.25122069599098 -15.834535739073813, 51.25122069599098 -15.845104900124415, 51.25122069599098 -15.876809061992976, 51.25122069599098 -15.887376007753604, 51.25122069599098 -15.919073515823465, 51.25122069599098 -15.940202410225474, 51.25122069599098 -15.971891578763547, 51.25122069599098 -15.993014905232737, 51.25122069599098 -16.003575731711145, 51.25122069599098 -16.024695709512564, 51.24023436786744 -16.098598008120117, 51.229248039743915 -16.1091532370364, 51.20727538349684 -16.172472806205846, 51.20727538349684 -16.193574824503596, 51.17431639912622 -16.225223619725952, 51.141357414756506 -16.32013945090704, 51.13037108663296 -16.35176784705497, 51.09741210226237 -16.4255475046925, 51.07543944601528 -16.446622269419848, 51.04248046164467 -16.50983282467138, 51.03149413352114 -16.530898421444643, 51.0205078053976 -16.541430358061664, 50.943603508533734 -16.657243643107517, 50.9326171804102 -16.688816953923748, 50.899658196039574 -16.730906587470887, 50.87768553979251 -16.751947920914766, 50.78979491480511 -16.878146992450425, 50.76782225855803 -16.888659785098586, 50.76782225855803 -16.89917199214655, 50.74584960231096 -16.941214957913296, 50.72387694606388 -16.962232918848187, 50.712890617940346 -16.983248528396203, 50.70190428981682 -17.004261783992508, 50.69091796169417 -17.0252726830764, 50.668945305447096 -17.0672874014624, 50.65795897732356 -17.0672874014624, 49.50439452435933 -17.759149753007183))"))