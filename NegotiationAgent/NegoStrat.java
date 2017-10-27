package ai2017;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.Actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import misc.Range;

public class NegoStrat {
	private SessionInfo sessionInfo;
	private double Umax = 1.0;
	private double Umin = 0.2;
	  

	  public NegoStrat(SessionInfo negoInfo){
		  this.sessionInfo = negoInfo;
	  }
	  
	  public BidDetails determineOpeningBid() {
		  return sessionInfo.getOutcomeSpace().getMaxBidPossible(); // maybe not a good strategy
	  }
	  
	  public BidDetails determineNextBid(AgentID lastSender) {
		  //Separate negotiation process to different time slot and apply different strategies.
		  double time = sessionInfo.getTimeline().getTime();
		  BidDetails nextbid = null;
		  if(time < 0.3D) {
			  nextbid = timeDependentBid(2);
		  }
		  return nextbid;
		  
	  }
	  
	  //Generate a random bid of utility higher than minUtil
	  public BidDetails randomBid(double minUtil) {
		  Range r = new Range(minUtil, Umax);
		  List <BidDetails> bids = sessionInfo.getOutcomeSpace().getBidsinRange(r);
		  int randNum = ThreadLocalRandom.current().nextInt(0, bids.size());
		  return bids.get(randNum);
	  }
	  
	  //time-dependent bid
	  public BidDetails timeDependentBid(double cFactor) {
		  double time = sessionInfo.getTimeline().getTime();
		  double targetUtility = p(time, cFactor);
		  BidDetails nextBid = sessionInfo.getOutcomeSpace().getBidNearUtility(targetUtility);
		  return nextBid;
	  }
	  
/*	  
	  //tradeoff bid
	  public BidDetails tradeOff(AgentID lastSender) {
		  int searchRange = 10;
		  assert sessionInfo.getMyBidHistory().size() > 0;
		  double myPrevBidUtil =  sessionInfo.getMyBidHistory().get(sessionInfo.getMyBidHistory().size()-1).getMyUndiscountedUtil();
		  List<BidDetails> allbids = sessionInfo.getOutcomeSpace().getAllOutcomes();
		  List<BidDetails> isoBids = new ArrayList<BidDetails>();
		  int index = sessionInfo.getOutcomeSpace().getIndexOfBidNearUtility(myPrevBidUtil);
		  //find a set of bids with same utility
		  for (int i = Math.max(0, index-searchRange); i < Math.min(index+searchRange, allbids.size()); i++) {
			  if( Math.abs(myPrevBidUtil - allbids.get(i).getMyUndiscountedUtil()) < 0.05) {
				  isoBids.add(allbids.get(i));
			  }
		  }
		  //check which bid is most similar to opponents bids.
		  List<Bid> opponentLastBid;
		  for(int i = 0; i < sessionInfo.getOpponentList().size(); i++) {
			  opponentLastBid.
			  }
		  }
		  
	  }
	*/  
	  public Actions myAcceptanceStrategy(Bid offeredBid)
	  {
		  double a = 1.02;
		  double b = 0.0;
		  double time = sessionInfo.getTimeline().getTime();
		  double opponentLastBidUtil = sessionInfo.getUtilitySpace().getUtility(offeredBid);
		  double myNextBidUtil = sessionInfo.getMyLastBid().getMyUndiscountedUtil();
		  double myPrevBidUtil = 1.0;
		  if(sessionInfo.getMyBidHistory().size() > 0) {
			  myPrevBidUtil = sessionInfo.getMyBidHistory().get(sessionInfo.getMyBidHistory().size()-2).getMyUndiscountedUtil();
		  }
		  if(opponentLastBidUtil > 0.88){
				return Actions.Accept;
			} 
		  else if(a*opponentLastBidUtil + b >= myNextBidUtil && time>0.95) {
			  return Actions.Accept;
			  }
		  else if(a*opponentLastBidUtil + b >= myPrevBidUtil && time>0.95) {
			  return Actions.Accept;
			  }
		  else
			  return Actions.Reject;
	  }
	  
	  public double concedeFuntion(double t, double cFactor) {
		  double ft = Math.pow(t, 1.0 / cFactor);
		  return ft;
	  }
	  
	  public double p(double t, double cFactor) {
		  return Umin + (Umax - Umin) * (1 - concedeFuntion(t, cFactor));
	  }

}
