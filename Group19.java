package ai2017;

import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.persistent.PersistentDataContainer;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.boaframework.Actions;
import negotiator.boaframework.SortedOutcomeSpace;
import ai2017.SessionInfo;
import ai2017.NegoStrat;


/**
 * This is your negotiation party.
 */
public class Group19 extends AbstractNegotiationParty {

	private Bid lastReceivedBid = null;
	private AgentID lastSender = null;
	private SortedOutcomeSpace sortedOutcome;
	private SessionInfo sessionInfo;
	private NegoStrat negoStrat;
	private int cnt = 0;
	
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
		System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());

		// if you need to initialize some variables, please initialize them below
		sortedOutcome = new SortedOutcomeSpace(info.getUtilitySpace());
		sessionInfo = new SessionInfo(info.getUtilitySpace(), info.getTimeline(), sortedOutcome);
		negoStrat = new NegoStrat(sessionInfo);
	}

	/**
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		BidDetails nextBid;
		if(lastReceivedBid == null || !validActions.contains(Accept.class)) {
			nextBid = negoStrat.determineOpeningBid();
			return new Offer(getPartyId(), nextBid.getBid());
		}
		else {
			//double prevMaxUtil = 0; //if(nextBid.getMyUndiscountedUtil() < prevMaxUtil)
			//determine next bid
			AgentID nextAgent = null;
			int nextOrderIdx = (sessionInfo.getOpponents().get(lastSender).getOrder() == sessionInfo.getOpponents().size()-1)? 
					0:sessionInfo.getOpponents().get(lastSender).getOrder()+1;
			for (AgentID agent:sessionInfo.getOpponents().keySet()) {
				if(sessionInfo.getOpponents().get(agent).getOrder() == nextOrderIdx) {
					nextAgent = agent;
					break;
				}
			}
			nextBid = negoStrat.determineNextBid(lastSender, nextAgent); 
			
			if(lastReceivedBid != null && 
					(negoStrat.myAcceptanceStrategy(lastReceivedBid, nextBid) == Actions.Accept)) {
				return new Accept(getPartyId(), lastReceivedBid);
			} 
			else {
				sessionInfo.getMyBidHistory().add(nextBid);
				return new Offer(getPartyId(), nextBid.getBid());	
			}
		}
	}
	
	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action. Can be null.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		if (action instanceof Offer) {
			lastReceivedBid = ((Offer) action).getBid();
			lastSender = sender;
		}
		if(sender == null)
			return;
		
		if(!sessionInfo.getOpponents().containsKey(sender)) {
			OpponentModel o = new OpponentModel(sessionInfo,sender);
			if (action instanceof Offer) {
				o.addBidToHistory(lastReceivedBid);
			}
			else if(action instanceof Accept) {
				o.addAcceptedBid(lastReceivedBid);	
			}
			o.setOrder(cnt);
			sessionInfo.getOpponents().put(sender, o);
			cnt += 1;
		}
		else {
			if (action instanceof Offer) {
				sessionInfo.getOpponents().get(sender).addBidToHistory(lastReceivedBid);
				if(sessionInfo.getOpponents().get(sender).getBidHistory().size() <= 100){
				    sessionInfo.getOpponents().get(sender).updateModel();
				}
			}
			else if(action instanceof Accept) {
				sessionInfo.getOpponents().get(sender).addAcceptedBid(lastReceivedBid);	
			}
		}	
	}

	@Override
	public String getDescription() {
		return "agent group 19";
	}

}
