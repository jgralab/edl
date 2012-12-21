package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.TermPrinter;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsAnnotatedSymbolTableOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsBodyTermOfPattern;
import de.uni_koblenz.edl.preprocessor.schema.grammar.IsFollowingSemanticActionOf;
import de.uni_koblenz.edl.preprocessor.schema.grammar.Pattern;
import de.uni_koblenz.edl.preprocessor.schema.section.SymbolTableDefinition;
import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.SemanticAction;
import de.uni_koblenz.edl.preprocessor.schema.term.Multiplicity;
import de.uni_koblenz.edl.preprocessor.schema.term.PatternTerm;
import de.uni_koblenz.jgralab.EdgeDirection;

public class FiniteStateAutomaton {

	private State initialState = new State();

	private final PatternValue patternValue;

	public FiniteStateAutomaton(Pattern pattern) {
		// determine the values of the current pattern
		SemanticAction semanticAction = null;
		boolean executeBefore = pattern.is_executeBefore();
		Set<SymbolTableDefinition> annotatedSymbolTables = new HashSet<SymbolTableDefinition>();

		for (IsFollowingSemanticActionOf ifsao : pattern
				.getIsFollowingSemanticActionOfIncidences(EdgeDirection.IN)) {
			assert semanticAction == null;
			semanticAction = ifsao.getAlpha();
		}

		for (IsAnnotatedSymbolTableOf iasto : pattern
				.getIsAnnotatedSymbolTableOfIncidences(EdgeDirection.IN)) {
			annotatedSymbolTables.add(iasto.getAlpha());
		}

		patternValue = new PatternValue(executeBefore, semanticAction,
				annotatedSymbolTables);

		// create NFA
		try {
			TermPrinter termPrinter = new TermPrinter();
			StringBuilder sb = new StringBuilder();
			termPrinter.convertTermWithoutPrefix(sb,
					pattern.get_headPatternTerm());
			Set<State> finalStates = new HashSet<State>();
			finalStates.add(initialState);
			finalStates = createAutomaton(finalStates, sb.toString(), 1, 1);
			for (IsBodyTermOfPattern ibtop : pattern
					.getIsBodyTermOfPatternIncidences(EdgeDirection.IN)) {
				sb = new StringBuilder();
				PatternTerm patternTerm = ibtop.getAlpha();
				termPrinter.convertTermWithoutPrefix(sb, patternTerm);
				Multiplicity multiplicity = patternTerm.get_multiplicity();
				if (multiplicity == null) {
					finalStates = createAutomaton(finalStates, sb.toString(),
							1, 1);
				} else {
					finalStates = createAutomaton(finalStates, sb.toString(),
							multiplicity.get_min(), multiplicity.get_max());
				}
			}
			for (State state : finalStates) {
				state.setFinalState(true);
			}
		} catch (IOException e) {
			// this should not happen
			e.printStackTrace();
		}
		// System.out.println(this);
		// transform into DFA
		transformIntoFiniteAutomaton();
		// System.out.println(this);
	}

	/**
	 * using Myhill construction
	 */
	private void transformIntoFiniteAutomaton() {
		Map<Set<State>, State> setOfStates2State = new HashMap<Set<State>, State>();
		Set<State> currentSet = new HashSet<State>();
		currentSet.add(initialState);
		State currentState = new State();
		initialState = currentState;
		setOfStates2State.put(currentSet, currentState);

		LinkedList<Set<State>> workingQueue = new LinkedList<Set<State>>();
		workingQueue.add(currentSet);
		while (!workingQueue.isEmpty()) {
			currentSet = workingQueue.pollFirst();
			currentState = setOfStates2State.get(currentSet);
			Map<String, Set<State>> nextStates = new HashMap<String, Set<State>>();
			// find next states of current state represented by currentSet
			for (State state : currentSet) {
				Set<SimpleTransition> transitions = new HashSet<SimpleTransition>();
				transitions.addAll(state.outgoingSimpleTransitions);
				transitions.addAll(state.wildCardTransitions);
				for (SimpleTransition st : transitions) {
					String label = st.acceptedTerm;
					Set<State> nextState = nextStates.get(label);
					if (nextState == null) {
						nextState = new HashSet<State>();
						nextStates.put(label, nextState);
					}
					st.endState.getEpsilonCompletion(nextState);
				}
			}
			Set<State> reachableStatesViaWildcards = nextStates.get("_");

			// create transitions
			for (Entry<String, Set<State>> entry : nextStates.entrySet()) {
				if (reachableStatesViaWildcards != null) {
					entry.getValue().addAll(reachableStatesViaWildcards);
				}
				State nextState = setOfStates2State.get(entry.getValue());
				if (nextState == null) {
					nextState = new State();
					for (State state : entry.getValue()) {
						if (state.isFinal()) {
							nextState.setFinalState(true);
							break;
						}
					}
					setOfStates2State.put(entry.getValue(), nextState);
					workingQueue.add(entry.getValue());
				}
				currentState.addTransition(new SimpleTransition(nextState,
						entry.getKey()));
			}
		}
	}

	private Set<State> createAutomaton(Set<State> states, String term,
			int minValue, int maxValue) {
		Set<State> result = new HashSet<State>();
		if (minValue == 0) {
			if (maxValue == 0) {
				result = states;
			} else if (maxValue == 1) {
				State nextState = new State();
				for (State state : states) {
					state.addTransition(new EpsilonTransition(nextState));
					state.addTransition(new SimpleTransition(nextState, term));
				}
				result.add(nextState);
			} else if (maxValue == Integer.MAX_VALUE) {
				State intermediateState = new State();
				State nextState = new State();
				for (State state : states) {
					state.addTransition(new EpsilonTransition(nextState));
					state.addTransition(new SimpleTransition(intermediateState,
							term));
				}
				intermediateState
						.addTransition(new EpsilonTransition(nextState));
				intermediateState.addTransition(new SimpleTransition(
						intermediateState, term));
				result.add(nextState);
			} else {
				State nextState = new State();
				result.add(nextState);
				for (State state : states) {
					result.add(state);
					state.addTransition(new SimpleTransition(nextState, term));
				}
				for (int i = 0; i < maxValue - 1; i++) {
					State next = new State();
					result.add(next);
					nextState.addTransition(new SimpleTransition(next, term));
					nextState = next;
				}
			}
		} else {
			assert minValue > 0;
			State nextState = new State();
			for (State state : states) {
				state.addTransition(new SimpleTransition(nextState, term));
			}
			minValue--;
			if (maxValue != Integer.MAX_VALUE) {
				maxValue--;
			}
			State state = nextState;
			while (minValue > 0) {
				nextState = new State();
				state.addTransition(new SimpleTransition(nextState, term));
				minValue--;
				if (maxValue != Integer.MAX_VALUE) {
					maxValue--;
				}
				state = nextState;
			}
			result.add(nextState);
			if (maxValue == Integer.MAX_VALUE) {
				nextState.addTransition(new SimpleTransition(nextState, term));
			} else {
				for (int i = 0; i < maxValue; i++) {
					State next = nextState.clone();
					nextState.addTransition(new SimpleTransition(next, term));
					result.add(next);
					nextState = next;
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<State> queue = new ArrayList<State>();
		queue.add(initialState);
		int nextIndex = 0;
		while (nextIndex < queue.size()) {
			State state = queue.get(nextIndex++);
			sb.append(state.toString());
			for (EpsilonTransition epsilonTransition : state.epsilonTransitions) {
				State nextState = epsilonTransition.endState;
				if (!queue.contains(nextState)) {
					queue.add(nextState);
				}
			}
			for (SimpleTransition trans : state.outgoingSimpleTransitions) {
				State nextState = trans.endState;
				if (!queue.contains(nextState)) {
					queue.add(nextState);
				}
			}
			for (SimpleTransition trans : state.wildCardTransitions) {
				State nextState = trans.endState;
				if (!queue.contains(nextState)) {
					queue.add(nextState);
				}
			}
		}
		return sb.toString();
	}

	public boolean matches(List<String> input) {
		State currentState = initialState;

		// System.out.print(currentState.getId());

		for (String nextInput : input) {
			currentState = currentState.proceed(nextInput);
			// System.out.print("--"
			// + nextInput
			// + "->"
			// + (currentState == null ? null
			// : (currentState.isFinal() ? "f" : "")
			// + currentState.getId()));
			if (currentState == null) {
				return false;
			}
		}
		// System.out.println();
		return currentState.isFinal();
	}

	public PatternValue getPatternValue() {
		return patternValue;
	}

}
