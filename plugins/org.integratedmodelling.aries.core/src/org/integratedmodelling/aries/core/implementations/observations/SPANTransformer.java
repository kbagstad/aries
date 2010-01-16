package org.integratedmodelling.aries.core.implementations.observations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.integratedmodelling.aries.core.ARIESNamespace;
import org.integratedmodelling.aries.core.span.SPANProxy;
import org.integratedmodelling.corescience.CoreScience;
import org.integratedmodelling.corescience.implementations.observations.Observation;
import org.integratedmodelling.corescience.interfaces.IObservationContext;
import org.integratedmodelling.corescience.interfaces.IState;
import org.integratedmodelling.corescience.interfaces.internal.TransformingObservation;
import org.integratedmodelling.corescience.literals.GeneralClassifier;
import org.integratedmodelling.modelling.ObservationFactory;
import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabInternalErrorException;
import org.integratedmodelling.thinklab.exception.ThinklabMalformedSemanticTypeException;
import org.integratedmodelling.thinklab.exception.ThinklabNoKMException;
import org.integratedmodelling.thinklab.exception.ThinklabResourceNotFoundException;
import org.integratedmodelling.thinklab.interfaces.annotations.InstanceImplementation;
import org.integratedmodelling.thinklab.interfaces.applications.ISession;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.knowledge.IInstance;
import org.integratedmodelling.utils.CamelCase;
import org.integratedmodelling.utils.Pair;
import org.integratedmodelling.utils.Polylist;

import clojure.lang.IFn;
import clojure.lang.LazyCons;

/**
 * Support for running the Clojure SPAN models and making lazy observation proxies to its
 * results.  
 * @author Ferdinando
 */
@InstanceImplementation(concept="aries:SPANTransformer")
public class SPANTransformer 
	extends Observation 
	implements TransformingObservation {
	
	private static SPANProxy span = null;
	
	// the following 5 fields are set at instance creation through reflection, as 
	// directed by SPANModel
	IConcept sourceConcept = null;
	IConcept sinkConcept = null;
	IConcept useConcept = null;
	IConcept flowConcept = null;
	IConcept flowDataConcept = null;
	Map<?,?> flowParams;
	
	/*
	 * called by the Clojure side to give us a bridge to the SPAN system
	 */
	public static void setSPANProxy(SPANProxy proxy) {
		span = proxy;
	}
	
	ArrayList<Pair<GeneralClassifier, IConcept>> classifiers = 
		new ArrayList<Pair<GeneralClassifier,IConcept>>();
	
	HashSet<IConcept> outputStates = new HashSet<IConcept>();
	IConcept cSpace = null;
	
	@Override
	public IObservationContext getTransformedContext(IObservationContext context)
			throws ThinklabException {
		return context;
	}

	@Override
	public IConcept getTransformedObservationClass() {
		return CoreScience.Observation();
	}
	
	/**
	 * Return the observable concept in the flow's concept space that matches the 
	 * keyword: e.g. if we get actual-source, we look for a child of the source 
	 * concept that is subsumed by the corresponding trait (eserv:ActualSource) and
	 * return it. If there's no such concept, we return null, meaning that this concept
	 * makes no sense for this model and therefore a state should not be created
	 * for it.
	 * 
	 * @param keyword
	 * @return
	 * @throws ThinklabNoKMException 
	 * @throws ThinklabResourceNotFoundException 
	 * @throws ThinklabMalformedSemanticTypeException 
	 */
	public IConcept defineObservable(String keyword) throws ThinklabException {

		String cn = "eserv:" + CamelCase.toUpperCamelCase(keyword, '-');
		IConcept concept = KnowledgeManager.get().requireConcept(cn);
		IConcept source = null;
		IConcept ret = null;
		
		if (concept.is(ARIESNamespace.SOURCE_TRAIT)) {
			source = sourceConcept;
		} else if (concept.is(ARIESNamespace.USE_TRAIT)) {
			source = useConcept;
		} else if (concept.is(ARIESNamespace.SINK_TRAIT)) {
			source = sinkConcept;
		} else if (concept.is(ARIESNamespace.FLOW_TRAIT)) {
			source = flowConcept;
		}
		
		if (source != null) {
			
			if (source.is(concept)) {
				ret = source;
			} else {
				for (IConcept ch : source.getChildren()) {
					if (ch.is(concept)) {
						ret = ch;
						break;
					}
				}
			}
		}
		
		return ret;
	}

	@Override
	public IInstance transform(IInstance sourceObs, ISession session,
			IObservationContext context) throws ThinklabException {
		
		/*
		 * run the proxy, obtain a map of closures 
		 * 
		 */
		Map<?,?> closures = 
			span.runSPAN(
				ObservationFactory.getObservation(sourceObs), 
				sourceConcept, 
				useConcept,
				sinkConcept, 
				flowDataConcept, 
				flowParams);
		
		/*
		 * create states from closures where the concept space of the observable
		 * contains an observable for the result type.
		 */
		ArrayList<Pair<IConcept, IState>> states = 
			new ArrayList<Pair<IConcept,IState>>();
		
// matrix version - add :matrix-list to the span-driver call in the SPAN proxy		
//		LazyCons matrices = (LazyCons) closures;
//		
//		for (Iterator<?> it = matrices.iterator(); it.hasNext(); ) {
//			Object res = it.next();
//			System.out.println("result is " + res);
//		}
		
		for (Object k : closures.keySet()) {
			
			IConcept observable = defineObservable(k.toString().substring(1));
			
			if (observable != null) {
				
				Object closure = closures.get(k);
				IFn clojure = (IFn) closure;
			
				try {
					
					// let's run it for now, we can make it lazy later
					Map<?,?> putaMadre = (Map<?, ?>) clojure.invoke();
					System.out.println("computed " + observable + " -> " + putaMadre);
				
				} catch (Exception e) {
					throw new ThinklabInternalErrorException(e);
				}
			} else {
				System.out.println("No observable defined for " + k + ": skipping");
			}
		}
		
		/*
		 * prepare new observation
		 */
		Polylist rdef = Polylist.list(
				CoreScience.OBSERVATION,
				Polylist.list(
						CoreScience.HAS_OBSERVABLE, getObservable().toList(null)));
		
		/*
		 * make new extents to match previous
		 */
		for (IConcept ext : context.getDimensions()) {
			rdef = ObservationFactory.addExtent(rdef, context.getExtent(ext).conceptualize());
		}
		
		/*
		 * add all dependents
		 */
		for (Pair<IConcept, IState> st : states) {
			
			Polylist ddef = Polylist.list(
					CoreScience.RANKING, // FIXME - should match the ds; ranking for now
					Polylist.list(
							CoreScience.HAS_OBSERVABLE, Polylist.list(st.getFirst())),
					Polylist.list(
							CoreScience.HAS_DATASOURCE, 
							st.getSecond().conceptualize()));
			
			rdef = ObservationFactory.addDependency(rdef, ddef);
		}
		
		return session.createObject(rdef);
		
	}
}