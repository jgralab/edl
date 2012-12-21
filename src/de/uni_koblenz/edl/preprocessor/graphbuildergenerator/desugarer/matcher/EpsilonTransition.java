package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.matcher;

public class EpsilonTransition extends Transition {

	public EpsilonTransition(State endState) {
		super(endState);
	}

	@Override
	protected boolean acceptsTerm(String input) {
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + " state " + endState.getId();
	}

}
