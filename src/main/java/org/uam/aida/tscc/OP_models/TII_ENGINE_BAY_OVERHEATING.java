/* This is an autogenerated PetriNet Java file. */
package org.uam.aida.tscc.OP_models;

import org.uam.aida.tscc.APFE.timeseries_guards.LogicalTSG;
import org.uam.aida.tscc.APFE.timeseries_guards.TSG;
import org.uam.aida.tscc.APFE.timeseries_guards.TrivialTSG;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.uam.aida.tscc.business.*;
import org.uam.aida.tscc.APFE.TSWFnet.TSWFNet;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;
import static org.uam.aida.tscc.APFE.timeseries_guards.LogicalTSG.LogicalExpression;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TII_ENGINE_BAY_OVERHEATING extends TSWFNet {

  private static final Logger LOG = Logger.getLogger(TII_ENGINE_BAY_OVERHEATING.class.getName());
  /** Custom declarations. */

  /** Places declaration. */
  private Place p10 = new Place("p10");
  private Place p0 = new Place("p0");
  private Place p1 = new Place("p1");
  private Place p2 = new Place("p2");
  private Place p3 = new Place("p3");
  private Place p4 = new Place("p4");
  private Place p5 = new Place("p5");
  private Place p6 = new Place("p6");
  private Place p7 = new Place("p7");
  private Place p8 = new Place("p8");
  private Place p9 = new Place("p9");

  /** Transitions declaration. */
  private Transition t12 = new Transition("t12","return true;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return true;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t11 = new Transition("t11","return true;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return true;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t14 = new Transition("t14","return true;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return true;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t13 = new Transition("t13","return false;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return false;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t16 = new Transition("t16","return false;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return false;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t15 = new Transition("t15","return true;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return true;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t18 = new Transition("t18","return false;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return false;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t17 = new Transition("t17","return true;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return true;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t19 = new Transition("t19","return true;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return true;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t21 = new Transition("t21","return true;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return true;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t20 = new Transition("t20","return true;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return true;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };
  private Transition t22 = new Transition("t22","return true;","currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp())") {
         public boolean evaluate(){
                 return true;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
         }
  };

  /** Input Arcs declaration. */
  private InputArc i34_p2_t14 = new InputArc("i34_p2_t14",p2,t14, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i47_p8_t20 = new InputArc("i47_p8_t20",p8,t20, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i31_p4_t16 = new InputArc("i31_p4_t16",p4,t16, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i35_p3_t14 = new InputArc("i35_p3_t14",p3,t14, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i23_p0_t11 = new InputArc("i23_p0_t11",p0,t11, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i37_p6_t17 = new InputArc("i37_p6_t17",p6,t17, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i29_p1_t13 = new InputArc("i29_p1_t13",p1,t13, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i28_p1_t12 = new InputArc("i28_p1_t12",p1,t12, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i30_p4_t15 = new InputArc("i30_p4_t15",p4,t15, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i39_p7_t18 = new InputArc("i39_p7_t18",p7,t18, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i43_p9_t21 = new InputArc("i43_p9_t21",p9,t21, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i45_p10_t22 = new InputArc("i45_p10_t22",p10,t22, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };
  private InputArc i40_p7_t19 = new InputArc("i40_p7_t19",p7,t19, "getTokenSet().size()>0" , "currentCase = getTokenSet().get(0)" ) {
         public boolean evaluate() {
                 return getTokenSet().size()>0;
         }
         public TokenSet execute() {
                 return new TokenSet(currentCase = getTokenSet().get(0));
         }
  };

  /** Output Arcs declaration. */
  private OutputArc o48_t22_p5 = new OutputArc("o48_t22_p5",p5,t22, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o36_t14_p6 = new OutputArc("o36_t14_p6",p6,t14, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o50_t16_p5 = new OutputArc("o50_t16_p5",p5,t16, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o41_t19_p8 = new OutputArc("o41_t19_p8",p8,t19, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o27_t11_p4 = new OutputArc("o27_t11_p4",p4,t11, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o46_t20_p5 = new OutputArc("o46_t20_p5",p5,t20, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o25_t11_p1 = new OutputArc("o25_t11_p1",p1,t11, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o42_t18_p9 = new OutputArc("o42_t18_p9",p9,t18, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o32_t12_p2 = new OutputArc("o32_t12_p2",p2,t12, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o49_t13_p5 = new OutputArc("o49_t13_p5",p5,t13, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o38_t17_p7 = new OutputArc("o38_t17_p7",p7,t17, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o44_t21_p10 = new OutputArc("o44_t21_p10",p10,t21, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };
  private OutputArc o33_t15_p3 = new OutputArc("o33_t15_p3",p3,t15, "currentCase.getObject()@currentCase.getTimestamp()" ){
         public TokenSet execute() {
                 return new TokenSet(currentCase.getObject(),currentCase.getTimestamp());
         }
  };

  /** Class TII_ENGINE_BAY_OVERHEATING constructor. */
  public TII_ENGINE_BAY_OVERHEATING() {
     setId("n0");
     setLabel("TII_ENGINE_BAY_OVERHEATING");
     setImportText("package OP_models;\n\nimport java.util.Collections;\nimport java.util.stream.Stream;\nimport java.util.Arrays;\nimport java.util.Collection;\nimport java.util.List;\nimport java.util.stream.Collectors;");
     p10.setLabel("p_4.2");
     addPlace(p10);
     p0.setLabel("p_i");
     addPlace(p0);
     p1.setLabel("p_1.1");
     addPlace(p1);
     p2.setLabel("p_aj1.1");
     addPlace(p2);
     p3.setLabel("p_aj1.2");
     addPlace(p3);
     p4.setLabel("p_1.2");
     addPlace(p4);
     p5.setLabel("p_e(OR-JOIN)");
     addPlace(p5);
     p6.setLabel("p_2");
     addPlace(p6);
     p7.setLabel("p_3");
     addPlace(p7);
     p8.setLabel("p_5");
     addPlace(p8);
     p9.setLabel("p_4.1");
     addPlace(p9);
     t12.setLabel("CHECK:S1.1:FULFILLED");
     t12.setGuardCondition(new LogicalTSG
(
	Arrays.asList
		(
			new LogicalExpression("58501.07","=",0),
			new LogicalExpression("58501.15","=",0)
		)
));
     addTransition(t12);
     t11.setLabel("AND-SPLIT");
     t11.setGuardCondition(new TrivialTSG());
     addTransition(t11);
     t14.setLabel("AND-JOIN");
     t14.setGuardCondition(new TrivialTSG());
     addTransition(t14);
     t13.setLabel("CHECK:S1.1:NOT FULFILLED");
     t13.setGuardCondition(new LogicalTSG
(
	Arrays.asList
		(
			new LogicalExpression("58501.07","=",0),
			new LogicalExpression("58501.15","=",0)
		)
).getComplementary());
     addTransition(t13);
     t16.setLabel("CHECK:S_1.2:NOT FULFILLED");
     t16.setGuardCondition(new LogicalTSG
(
	Arrays.asList
		(
			new LogicalExpression("55253.02",">",70)
		)
).getComplementary());
     addTransition(t16);
     t15.setLabel("CHECK:S_1.2:FULFILLED");
     t15.setGuardCondition(new LogicalTSG
(
	Arrays.asList
		(
			new LogicalExpression("55253.02",">",70)
		)
));
     addTransition(t15);
     t18.setLabel("SUPERVISION:S_3:NOT FULFILLED");
     t18.setGuardCondition(new LogicalTSG
(
	Arrays.asList
		(
			new LogicalExpression("55254.04","=",3)
		)
).getComplementary());
     addTransition(t18);
     t17.setLabel("ACTION:S_2");
     t17.setGuardCondition(new TSG() {
	@Override
	public boolean evaluate(Collection<LogEntry> logEntries) {
		Stream<Double> distinctAltitudes=
			logEntries.stream()
			.filter(e -> e.getVarname().equals("2002.43.05"))
			.map(LogEntry::getRecordAsDouble)
			.distinct();
		
		return (distinctAltitudes.count() > 1);
	}

	@Override
	public Collection<String> getInvolvedVariables() {
		return Arrays.asList("2002.43.05");
	}
});
     addTransition(t17);
     t19.setLabel("SUPERVISION:S_3:FULFILLED");
     t19.setGuardCondition(new LogicalTSG
(
	Arrays.asList
		(
			new LogicalExpression("55254.04","=",3)
		)
));
     addTransition(t19);
     t21.setLabel("ACTION:S_4.1");
     t21.setGuardCondition(new LogicalTSG
(
	Arrays.asList
		(
			new LogicalExpression("2001.43.15","=",4),
			new LogicalExpression("2002.42.04","=",33)
		)
));
     addTransition(t21);
     t20.setLabel("ACTION:S_5");
     t20.setGuardCondition(new LogicalTSG
(
	Arrays.asList
		(
			new LogicalExpression("2001.42.04","=",33)
		)
));
     addTransition(t20);
     t22.setLabel("ACTION:S_4.2");
     t22.setGuardCondition(new LogicalTSG
(
	Arrays.asList
		(
			new LogicalExpression("2002.42.04","=",19)
		)
));
     addTransition(t22);
     i34_p2_t14.setEvaluateText("getTokenSet().size()>0");
     i34_p2_t14.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i34_p2_t14);
     i47_p8_t20.setEvaluateText("getTokenSet().size()>0");
     i47_p8_t20.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i47_p8_t20);
     i31_p4_t16.setEvaluateText("getTokenSet().size()>0");
     i31_p4_t16.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i31_p4_t16);
     i35_p3_t14.setEvaluateText("getTokenSet().size()>0");
     i35_p3_t14.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i35_p3_t14);
     i23_p0_t11.setEvaluateText("getTokenSet().size()>0");
     i23_p0_t11.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i23_p0_t11);
     i37_p6_t17.setEvaluateText("getTokenSet().size()>0");
     i37_p6_t17.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i37_p6_t17);
     i29_p1_t13.setEvaluateText("getTokenSet().size()>0");
     i29_p1_t13.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i29_p1_t13);
     i28_p1_t12.setEvaluateText("getTokenSet().size()>0");
     i28_p1_t12.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i28_p1_t12);
     i30_p4_t15.setEvaluateText("getTokenSet().size()>0");
     i30_p4_t15.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i30_p4_t15);
     i39_p7_t18.setEvaluateText("getTokenSet().size()>0");
     i39_p7_t18.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i39_p7_t18);
     i43_p9_t21.setEvaluateText("getTokenSet().size()>0");
     i43_p9_t21.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i43_p9_t21);
     i45_p10_t22.setEvaluateText("getTokenSet().size()>0");
     i45_p10_t22.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i45_p10_t22);
     i40_p7_t19.setEvaluateText("getTokenSet().size()>0");
     i40_p7_t19.setExecuteText("currentCase = getTokenSet().get(0)");
     addInputArc(i40_p7_t19);
     addOutputArc(o48_t22_p5);
     addOutputArc(o36_t14_p6);
     addOutputArc(o50_t16_p5);
     addOutputArc(o41_t19_p8);
     addOutputArc(o27_t11_p4);
     addOutputArc(o46_t20_p5);
     addOutputArc(o25_t11_p1);
     addOutputArc(o42_t18_p9);
     addOutputArc(o32_t12_p2);
     addOutputArc(o49_t13_p5);
     addOutputArc(o38_t17_p7);
     addOutputArc(o44_t21_p10);
     addOutputArc(o33_t15_p3);
  }

}
