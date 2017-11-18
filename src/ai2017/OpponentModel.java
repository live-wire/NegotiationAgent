package ai2017;

import java.util.ArrayList;
import java.util.HashMap;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.utility.AdditiveUtilitySpace;


public class OpponentModel {
	public AgentID agentId;
	private int order = 0;
	private ArrayList<Bid> bids;
	private ArrayList<Bid> acceptedBids;
	private HashMap <Issue, Double> issueWeights;
	private HashMap<Issue, HashMap<Value, Integer>> valueFreq;
	private AdditiveUtilitySpace opponentUtilitySpace;
	private double addWeight = 0.1;
	
	public OpponentModel(SessionInfo sessionInfo, AgentID agentid)
	{
		agentId = agentid;
		bids = new ArrayList<Bid>();
		acceptedBids = new ArrayList<Bid>();
		opponentUtilitySpace = new AdditiveUtilitySpace(sessionInfo.getDomain());
		issueWeights = new HashMap<Issue, Double>();
		valueFreq = new HashMap<Issue, HashMap<Value, Integer>>();
		this.initModel();
	}
	
	public void initModel() {
		double numOfIssues = opponentUtilitySpace.getDomain().getIssues().size();
		//initialize issue weights to 1/(number of issues) (normalized)
		//initialize valueFreq in each issue to 0
		for(Issue issue: opponentUtilitySpace.getDomain().getIssues()) {
			issueWeights.put(issue, 1.0D/numOfIssues);
			valueFreq.put(issue, new HashMap<Value, Integer>());
			IssueDiscrete discreteIssue = (IssueDiscrete) issue;
			for(Value value:discreteIssue.getValues()) {
				valueFreq.get(discreteIssue).put(value, 0);
			}
		}
	}
	
	public void updateModel() {
		Bid currentBid = bids.get(bids.size()-1);
		for(Issue issue: opponentUtilitySpace.getDomain().getIssues()) {
			IssueDiscrete discreteIssue = (IssueDiscrete) issue;
			//update valueFreq
			for(Value value: discreteIssue.getValues()) {
				if(value.equals(currentBid.getValue(issue.getNumber()))) {
					valueFreq.get(discreteIssue).put(value, valueFreq.get(discreteIssue).get(value)+1);
				}
			}
		}
		if(bids.size() < 2) { //cannot compare yet
			return;
		}
		
		Bid lastBid = bids.get(bids.size()-2);
		HashMap<Issue, Integer> isValueChanged = new HashMap<Issue, Integer>();
		int changed = 0;
		int numOfChanged = 0;
		for(Issue issue: opponentUtilitySpace.getDomain().getIssues()) {
			changed = (currentBid.getValue(issue.getNumber()).equals(lastBid.getValue(issue.getNumber())))? 0:1;
			if (changed == 1) {
				numOfChanged ++;
			}
			isValueChanged.put(issue, changed);
		}
		//update issue weight
		for(Issue issue: opponentUtilitySpace.getDomain().getIssues()) {
			issueWeights.put(issue, (issueWeights.get(issue) + isValueChanged.get(issue)*addWeight)/(1+addWeight*numOfChanged));
		}
	}
	
	public double getOpponentUtility(Bid bid) {
		double util = 0;
		HashMap<Value,Double> evaluationValue = new HashMap<Value, Double>();
		for(Issue issue: opponentUtilitySpace.getDomain().getIssues()) {
			evaluationValue = calcEvaValue(valueFreq.get(issue));
			util += issueWeights.get(issue) * evaluationValue.get(bid.getValue(issue.getNumber()));
		}
		return util;
	}
	
	public HashMap<Value, Double> calcEvaValue(HashMap<Value,Integer> valueFreq) {
		double maxValue = 0D;
		HashMap<Value, Double> evaluationValue = new HashMap<Value, Double>();
		for(Value value: valueFreq.keySet()) {
			if(valueFreq.get(value) > maxValue) {
				maxValue = valueFreq.get(value);
			}
		}
		for(Value value: valueFreq.keySet()) {
			evaluationValue.put(value, (double)valueFreq.get(value)/maxValue);
		}
		return evaluationValue;
	}
	
	
	public void addBidToHistory(Bid bid) {
		bids.add(bid);
	}
	public ArrayList<Bid> getBidHistory(){
		return bids;
	}
	public void addAcceptedBid(Bid bid) {
		acceptedBids.add(bid);
	}
	public ArrayList<Bid> getAcceptedBid(){
		return acceptedBids;
	}
	public Bid getLastBid() {
		return bids.get(bids.size()-1);
	}
	public AgentID getAgentID() {
		return this.agentId;
	}
	public void setOrder(int o) {
		order = o;
	}
	public int getOrder() {
		return order;
	}

}
