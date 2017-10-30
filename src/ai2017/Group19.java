package ai2017;

import java.util.ArrayList;
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
			double prevMaxUtil = 0;
			nextBid = negoStrat.determineNextBid(lastSender);
			if(nextBid.getMyUndiscountedUtil() < prevMaxUtil) {
				
			}
			sessionInfo.getMyBidHistory().add(nextBid);
			if(lastReceivedBid != null && 
					(negoStrat.myAcceptanceStrategy(lastReceivedBid) == Actions.Accept)) {
				return new Accept(getPartyId(), lastReceivedBid);
			} 
			else {
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
		
		
		boolean exist = false;
		//create or update opponent model
		for(int i=0; i < sessionInfo.getOpponentList().size(); i++) {
			if(sessionInfo.getOpponentList().get(i).getAgentID().equals(sender)) {
				exist = true;
				if (action instanceof Offer || action instanceof Accept) {
					sessionInfo.getOpponentList().get(i).addBidToHistory(lastReceivedBid);
					sessionInfo.getOpponentList().get(i).updateModel();
				}
			}	
		}
		if(!exist) {
			OpponentModel o = new OpponentModel(sessionInfo,sender);
			if (action instanceof Offer || action instanceof Accept) {
				o.addBidToHistory(lastReceivedBid);
			}
			sessionInfo.getOpponentList().add(o);
		}
	}

	@Override
	public String getDescription() {
		return "agent group 19";
	}

}
