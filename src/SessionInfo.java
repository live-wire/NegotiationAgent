package ai2017;

import java.util.ArrayList;
import java.util.List;

import negotiator.Domain;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.OutcomeSpace;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;

public class SessionInfo {
	protected OutcomeSpace outcomeSpace;	
	protected AbstractUtilitySpace utilitySpace;
	protected TimeLineInfo timeline;
	private List<BidDetails> myBidHistory;
	private List<OpponentModel> opponentList;

	protected SessionInfo() {}
	  

	public SessionInfo(AbstractUtilitySpace utilitySpace, TimeLineInfo timeline, OutcomeSpace outcomeSpace)
	{
	    this.utilitySpace = utilitySpace;
	    this.timeline = timeline;
	    this.outcomeSpace = outcomeSpace;
	    myBidHistory = new ArrayList<BidDetails>();
	    opponentList = new ArrayList<OpponentModel>();
	}
	public List<OpponentModel> getOpponentList(){
		return opponentList;
	}
	public List<BidDetails> getMyBidHistory(){
	    return myBidHistory;
	}
	public BidDetails getMyLastBid() {
		return myBidHistory.get(myBidHistory.size()-1);
	}


	  public TimeLineInfo getTimeline()
	  {
	    return timeline;
	  }

	  public Domain getDomain()
	  {
	    if (utilitySpace != null) {
	      return utilitySpace.getDomain();
	    }
	    return null;
	  }

	  public AbstractUtilitySpace getUtilitySpace()
	  {
	    return utilitySpace;
	  }

	  public OutcomeSpace getOutcomeSpace()
	  {
	    return outcomeSpace;
	  }
}
