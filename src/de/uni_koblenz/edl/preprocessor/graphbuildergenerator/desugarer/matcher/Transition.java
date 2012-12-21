package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.matcher;


public abstract class Transition {

	protected State endState;

	public Transition(State endState) {
		this.endState = endState;
	}

	protected abstract boolean acceptsTerm(String input);

	@Override
	public String toString() {
		return "-->";
	}
}
