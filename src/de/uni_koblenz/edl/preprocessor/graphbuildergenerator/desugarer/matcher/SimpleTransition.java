package de.uni_koblenz.edl.preprocessor.graphbuildergenerator.desugarer.matcher;

public class SimpleTransition extends Transition {

	protected String acceptedTerm;

	public SimpleTransition(State endState, String acceptedTerm) {
		super(endState);
		this.acceptedTerm = acceptedTerm;
	}

	@Override
	protected boolean acceptsTerm(String input) {
		return acceptedTerm.equals("_") || acceptedTerm.equals(input);
	}

	@Override
	public String toString() {
		return super.toString() + "{" + acceptedTerm + "} state "
				+ endState.getId();
	}

}
