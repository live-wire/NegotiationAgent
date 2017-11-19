package ai2017;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.Actions;
import negotiator.issue.Issue;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import misc.Range;

import javax.print.attribute.standard.MediaSize;

public class NegoStrat {
	private SessionInfo sessionInfo;
	private double minTimeUtility = 0.7;  
	private double minRandUtility = 0.7;
	private double MIN_SELF_KALAI_UTILITY = 0.7;
	private double cRate = 2;
	private int tradeOffSearchRange = 5;
	private int cnt = 0;

	  public NegoStrat(SessionInfo negoInfo){
		  this.sessionInfo = negoInfo;
	  }
	  
	  public BidDetails determineOpeningBid() {
		  return sessionInfo.getOutcomeSpace().getMaxBidPossible(); // maybe not a good strategy
	  }
	  
	  public BidDetails determineNextBid(AgentID lastSender, AgentID nextAgent) {
		  //Separate negotiation process to different time slot and apply different strategies.
		  
		  //double time = sessionInfo.getTimeline().getTime();
		  boolean reliableModel = (sessionInfo.getOpponents().get(lastSender).getBidHistory().size() >= 10) ? true : false;
		  BidDetails nextbid = null;
		  cnt += 1;
		  // Give random bids from time to time
		  /*if(cnt % 20 == 1) {
			  nextbid = randomBid();
		  }
		  else if(reliableModel && cnt % 3 == 1) {
			  nextbid = tradeOff(true, nextAgent);
		  }
		  else {*/
			  nextbid = timeDependentBid(cRate);
		  //}
		  return nextbid;
	  }
	  
	  //Generate a random bid of utility higher than minRandUtility
	  public BidDetails randomBid() {
		  Range r = new Range(minRandUtility, 1.0);
		  List <BidDetails> bids = sessionInfo.getOutcomeSpace().getBidsinRange(r);
		  int randNum = ThreadLocalRandom.current().nextInt(0, bids.size());
		  return bids.get(randNum);
	  }
	  
	  //Generate a time-dependent bid of utility higher than minTimeUtility and a concession rate of cRate
	  public BidDetails timeDependentBid(double cFactor) {
		  double time = sessionInfo.getTimeline().getTime();
		  double targetUtility = bidUtil(time, cFactor);
		  BidDetails nextBid = sessionInfo.getOutcomeSpace().getBidNearUtility(targetUtility);
		  return nextBid;
	  }

	  //Generating a bid based on opponent models
	  public BidDetails kalaiOptimalBid(double alpha){


		  boolean NASH = false;



		  double bestUtil = -1;
		  List<BidDetails> allbids = sessionInfo.getOutcomeSpace().getAllOutcomes();
		  HashMap<AgentID,OpponentModel> opponents =  sessionInfo.getOpponents();

		  //Just Iterating over opponents and for each opponent finding the bid with the least utility
		  Iterator it = opponents.entrySet().iterator();
		  HashMap<BidDetails,Double> respectiveMinimumUtilities = new HashMap<BidDetails,Double>();

		  double maxproduct = Double.MIN_VALUE;
		  BidDetails nashBid = null;
		  while (it.hasNext()) {
			  Map.Entry pair = (Map.Entry)it.next();
			  double minOpponentUtility = Double.MAX_VALUE;
			  BidDetails minOpponentBid = allbids.get(0);

			  //Since allbids are sorted based on our agent's utility, let's just multiply the utility of the opponents in each iteration by
			  //say 1.001 since we're minimizing first
			  alpha = 1;

			  for (BidDetails bid:allbids){


			  	double selfutility = sessionInfo.getUtilitySpace().getUtility(bid.getBid());


				double utilbid =   ((OpponentModel)pair.getValue()).getOpponentUtility(bid.getBid());
				utilbid = utilbid * alpha;

				if(utilbid<minOpponentUtility)
				{
					minOpponentBid = bid;
					minOpponentUtility = utilbid;
				}
				if(selfutility<MIN_SELF_KALAI_UTILITY)
					break;

				//Trying to calculate the Nash Equilibrium here:
				double product = selfutility;
				Iterator inneriterator = opponents.entrySet().iterator();
				  while (inneriterator.hasNext()) {
					  Map.Entry innerpair = (Map.Entry)inneriterator.next();
					  product = product * (double)innerpair.getValue();
				  }
				 if(product > maxproduct)
				 {
				 	maxproduct = product;
				 	nashBid = bid;
				 }

			  }
			  respectiveMinimumUtilities.put(minOpponentBid,minOpponentUtility);
		  }
		  //Now get maximum bid utility of the minimums
		  Iterator it2 = respectiveMinimumUtilities.entrySet().iterator();
		  BidDetails bidFinal = null;
		  double utilMax = Double.MIN_VALUE;
		  while (it2.hasNext()) {
			  Map.Entry pair = (Map.Entry) it2.next();
			  if(((double)pair.getValue())>utilMax)
			  {
			  	utilMax = (double)pair.getValue();
			  	bidFinal = (BidDetails) pair.getKey();
			  }
		  }

		  if(nashBid!=null && NASH && (sessionInfo.getUtilitySpace().getUtility(nashBid.getBid()) > sessionInfo.getUtilitySpace().getUtility(bidFinal.getBid())))
		  {
			  return nashBid;
		  }
		  //This will have a self utility of atleast MIN_SELF_KALAI_UTILITY and should be almost accepted by most parties
		  return bidFinal;
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
		  //find a set of bids with same utility
		  for (int i = Math.max(0, index - tradeOffSearchRange); i < Math.min(index + tradeOffSearchRange, allbids.size()); i++) {
			  if( Math.abs(myPrevBidUtil - allbids.get(i).getMyUndiscountedUtil()) < 0.05D) {
				  isoBids.add(allbids.get(i));
			  }
		  }
		  //check which bid is most similar to opponents bids.
		  //Average = true: consider distance to all opponents
		  //Average = false: consider distance to only next opponent
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
	  
	  //Calculate Hamming Distance between two bids
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
