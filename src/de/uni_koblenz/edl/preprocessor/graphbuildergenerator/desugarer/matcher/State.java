package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class State implements Cloneable {

	private static long nextId = 1;

	private final long id = nextId++;

	private boolean isFinalState = false;

	List<SimpleTransition> outgoingSimpleTransitions = new ArrayList<SimpleTransition>(
			3);

	List<EpsilonTransition> epsilonTransitions = new ArrayList<EpsilonTransition>();

	List<SimpleTransition> wildCardTransitions = new ArrayList<SimpleTransition>(
			1);

	@Override
	public State clone() {
		State duplicate = new State();
		duplicate.isFinalState = isFinalState;
		for (SimpleTransition st : outgoingSimpleTransitions) {
			duplicate.outgoingSimpleTransitions.add(st);
		}
		for (EpsilonTransition et : epsilonTransitions) {
			duplicate.epsilonTransitions.add(et);
		}
		return duplicate;
	}

	public void addTransition(Transition transition) {
		if (transition instanceof EpsilonTransition) {
			assert epsilonTransitions.isEmpty();
			epsilonTransitions.add((EpsilonTransition) transition);
		} else {
			if (((SimpleTransition) transition).acceptedTerm.equals("_")) {
				wildCardTransitions.add((SimpleTransition) transition);
			} else {
				outgoingSimpleTransitions.add((SimpleTransition) transition);
			}
		}
	}

	public void setFinalState(boolean isFiniteState) {
		this.isFinalState = isFiniteState;
	}

	public boolean isFinal() {
		return isFinalState;
	}

	public State proceed(String input) {
		for (SimpleTransition transition : outgoingSimpleTransitions) {
			if (transition.acceptsTerm(input)) {
				return transition.endState;
			}
		}
		if (!wildCardTransitions.isEmpty()) {
			assert wildCardTransitions.size() == 1;
			return wildCardTransitions.get(0).endState;
		}
		return null;
	}

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (isFinalState) {
			sb.append("final ");
		}
		sb.append("state ").append(id).append(":\n");
		for (EpsilonTransition epsilonTransition : epsilonTransitions) {
			sb.append("\t").append(epsilonTransition).append("\n");
		}
		for (SimpleTransition st : outgoingSimpleTransitions) {
			sb.append("\t").append(st).append("\n");
		}
		for (SimpleTransition st : wildCardTransitions) {
			sb.append("\t").append(st).append("\n");
		}
		return sb.toString();
	}

	public void getEpsilonCompletion(Set<State> nextState) {
		nextState.add(this);
		for (EpsilonTransition et : epsilonTransitions) {
			if (!nextState.contains(et.endState)) {
				et.endState.getEpsilonCompletion(nextState);
			}
		}
	}

}
