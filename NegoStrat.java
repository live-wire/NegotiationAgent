package ai2017;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.Actions;
import negotiator.issue.Issue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import misc.Range;

public class NegoStrat {
	private SessionInfo sessionInfo;
	private double minTimeUtility = 0.6;  
	private double minRandUtility = 0.7;
	private double cRate = 2;
	private int tradeOffSearchRange = 5;
	private int cnt = 0;
	private boolean reliableOpponentModel = false;

	  public NegoStrat(SessionInfo negoInfo){
		  this.sessionInfo = negoInfo;
	  }
	  
	  public BidDetails determineOpeningBid() {
		  return sessionInfo.getOutcomeSpace().getMaxBidPossible(); // maybe not a good strategy
	  }
	  
	  public BidDetails determineNextBid(AgentID lastSender, AgentID nextAgent) {
		  //If deadline is near, give best bid from opponent history
		  //Separate negotiation process to different time slot and apply different strategies.
		  double time = sessionInfo.getTimeline().getTime();
		  //check whether opponent model is reliable
		  if(sessionInfo.getOpponents().get(lastSender).getBidHistory().size() >= 30) {
			  reliableOpponentModel = true;
		  }
		  BidDetails nextbid = null;
		  cnt += 1;
		  // Give random bids from time to time
		  if(cnt % 20 == 1) {
			  nextbid = randomBid();
		  }
		  else if(reliableOpponentModel && cnt % 3 == 1) {
			  nextbid = tradeOff(true, nextAgent);
		  }
		  else {
			  nextbid = timeDependentBid(cRate);
		  }
		  return nextbid;
	  }
	  
	  //Generate a random bid of utility higher than minRandUtility
	  public BidDetails randomBid() {
		  Range r = new Range(minRandUtility, 1.0);
		  List <BidDetails> bids = sessionInfo.getOutcomeSpace().getBidsinRange(r);
		  int randNum = ThreadLocalRandom.current().nextInt(0, bids.size());
		  return bids.get(randNum);
	  }
	  
	  //Generate a time-dependent bid with utility higher than minTimeUtility and concession rate cRate
	  public BidDetails timeDependentBid(double cFactor) {
		  double time = sessionInfo.getTimeline().getTime();
		  double targetUtility = bidUtil(time, cFactor);
		  BidDetails nextBid = sessionInfo.getOutcomeSpace().getBidNearUtility(targetUtility);
		  return nextBid;
	  }
	  
	  //Generate a tradeoff bid
	  public BidDetails tradeOff(boolean average, AgentID nextAgent) {
		  BidDetails nextBid = null;
		  if(sessionInfo.getMyBidHistory().size() == 0)
			  return nextBid;
		  double myPrevBidUtil =  sessionInfo.getMyBidHistory().get(sessionInfo.getMyBidHistory().size()-1).getMyUndiscountedUtil();
		  List<BidDetails> allbids = sessionInfo.getOutcomeSpace().getAllOutcomes();
		  List<BidDetails> isoBids = new ArrayList<BidDetails>();
		  int index = sessionInfo.getOutcomeSpace().getIndexOfBidNearUtility(myPrevBidUtil);
		  //find a set of bids with same utility with maximum concession of 0.05
		  for (int i = Math.max(0, index - tradeOffSearchRange); i < Math.min(index + tradeOffSearchRange, allbids.size()); i++) {
			  if( Math.abs(myPrevBidUtil - allbids.get(i).getMyUndiscountedUtil()) < 0.05D) {
				  isoBids.add(allbids.get(i));
			  }
		  }
		  //check which bid in the isoBids list is most similar to opponents bids
		  //Average = true: evaluate similarity based on averaging all opponents
		  //Average = false: evaluate similarity based on the next opponent only
		  int minDist = sessionInfo.getOpponents().size()*sessionInfo.getDomain().getIssues().size();
		  for(BidDetails bid: isoBids) {
			  int localDist = 0;
			  if(average) {
				  for(AgentID agent : sessionInfo.getOpponents().keySet()) {
					  localDist += bidDistance(bid.getBid(), sessionInfo.getOpponents().get(agent).getLastBid());
			      }
			  }
			  else {
				  localDist += bidDistance(bid.getBid(), sessionInfo.getOpponents().get(nextAgent).getLastBid());
			  }
			  if(localDist < minDist) {
				  minDist = localDist;
				  nextBid = bid;
			  }
		  }
		  return nextBid;
	  }

	  //Evaluate Hamming distance of two bids
      public int bidDistance (Bid bid1, Bid bid2) {
    	  int dist = 0;
    	  for(Issue issue: sessionInfo.getDomain().getIssues()) {
  			dist += bid1.getValue(issue.getNumber()).equals(bid2.getValue(issue.getNumber())) ? 0:1;
    	  }
    	  return dist;
     }
	
	  public Actions myAcceptanceStrategy(Bid offeredBid, BidDetails myBid){
		  double a = 1.02;
		  double b = 0.0;
		  //double time = sessionInfo.getTimeline().getTime();
		  double opponentLastBidUtil = sessionInfo.getUtilitySpace().getUtility(offeredBid);
		  double myNextBidUtil = myBid.getMyUndiscountedUtil();
		  double myPrevBidUtil = 1.0;
		  if(sessionInfo.getMyBidHistory().size() > 0) {
			  myPrevBidUtil = sessionInfo.getMyBidHistory().get(sessionInfo.getMyBidHistory().size()-1).getMyUndiscountedUtil();
		  }
		  if(opponentLastBidUtil > 0.88){
			  return Actions.Accept;
			} 
		  else if(a*opponentLastBidUtil + b >= myNextBidUtil) {
			  return Actions.Accept;
			  }
		  else if(a*opponentLastBidUtil + b >= myPrevBidUtil) {
			  return Actions.Accept;
			  }
		  else
			  return Actions.Reject;
	  }
	  
	  //Time-dependent concession function
	  public double polynomialConcession(double t, double beta) {
		  double ft = Math.pow(t, 1.0 / beta);
		  return ft;
	  }	  
	  public double bidUtil(double t, double beta) {
		  return minTimeUtility + (1.0D - minTimeUtility) * (1 - polynomialConcession(t, beta));
	  }

}
