package ginrummy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;

public class Cost_Payoff_Player implements GinRummyPlayer{
	private Stack<Card> discard = new Stack<Card>();
	protected int playerNum;
	@SuppressWarnings("unused")
	protected int startingPlayerNum;
	protected ArrayList<Card> cards = new ArrayList<Card>();
	protected Random random = new Random();
	protected boolean opponentKnocked = false;
	Card faceUpCard, drawnCard; 
	ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();

	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.cards.clear();
		for (Card card : cards)
			this.cards.add(card);
		opponentKnocked = false;
		drawDiscardBitstrings.clear();
	}

	@Override
	public boolean willDrawFaceUpCard(Card card) {
		int set_payoff = 0; int run_payoff = 0;
		// Return true if card would be a part of a meld, false otherwise.
		this.faceUpCard = card;
		@SuppressWarnings("unchecked")
		ArrayList<Card> newCards = (ArrayList<Card>) cards.clone();
		newCards.add(card);
		
		// Calculates payoff of the faceup card
		for(Card c : cards) {
			
			if(this.faceUpCard.getRank() - c.getRank() == 0)
				set_payoff++;
			
			else if(((this.faceUpCard.getRank() - c.getRank()) == -1) || this.faceUpCard.getRank() - c.getRank() == 1 )
				run_payoff++;
			
		}
		
		
		for (ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(newCards))
			if (meld.contains(card) || set_payoff >= 1 || run_payoff >= 1)
				return true;
		return false;
	}

	@Override
	public void reportDraw(int playerNum, Card drawnCard) {
		// Ignore other player draws.  Add to cards if playerNum is this player.
		if (playerNum == this.playerNum) {
			cards.add(drawnCard);
			this.drawnCard = drawnCard;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Card getDiscard() {
		// Discard a random card (not just drawn face up) leaving minimal deadwood points.
		int minDeadwood = Integer.MAX_VALUE;
		ArrayList<Card> candidateCards = new ArrayList<Card>();
		ArrayList<Integer> diffCosts = new ArrayList<Integer>();
		
		for (Card card : cards) {
			// Cannot draw and discard face up card.
			if (card == drawnCard && drawnCard == faceUpCard)
				continue;
			// Disallow repeat of draw and discard.
			ArrayList<Card> drawDiscard = new ArrayList<Card>();
			drawDiscard.add(drawnCard);
			drawDiscard.add(card);
			if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard)))
				continue;
			
			ArrayList<Card> remainingCards = (ArrayList<Card>) cards.clone();
			//remainingCards.remove(card);
			ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(remainingCards);
			
			int deadwood = bestMeldSets.isEmpty() ? GinRummyUtil.getDeadwoodPoints(remainingCards) : GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
			
			int cost=0;	
			int point = 0;
			
			System.out.println("Card Rank: " + card.getRank());
			System.out.println("Card Suit: " + card.getSuit());
			System.out.println();
//			ArrayList<Card> potentialCards = (ArrayList<Card>) remainingCards.clone();
			
			
			for(int i = 0; i < remainingCards.size(); i++) {
				
				
				System.out.println("Remaining Rank: " + remainingCards.get(i).getRank());
				System.out.println("Remaining Suit: " + remainingCards.get(i).getSuit());
				
				//same suit, diff rank, cost 0
				if (card.getSuit() == remainingCards.get(i).getSuit()) {
					System.out.println("Same suit: " + card + "," + remainingCards.get(i));
					point = Math.abs(card.getRank() - remainingCards.get(i).getRank());
					cost += point;
				}
				
				//if card contains in sets
				else if (!bestMeldSets.isEmpty() && bestMeldSets.get(0).get(0).contains(card)) {
					
					System.out.println("contain in sets: " + card + "," + remainingCards.get(i));
					point = -100;
					cost += point;
				
				}
				
				//diff suit, same rank, cost 0
				else if (card.getRank() == remainingCards.get(i).getRank()) {
					System.out.println("Same rank: " + card + "," + remainingCards.get(i));
					point = 0;
					cost += point;
					
				}
				//anything else cost +50
				else {
					System.out.println("Else: " + card + "," + remainingCards.get(i));
					point = 50;
					cost += point;
//					candidateCards.clear();
//					candidateCards.add(card);
				}
				
				if(!bestMeldSets.isEmpty()) {
					System.out.println("best meld contains: " + bestMeldSets.get(0).get(0).contains(card));
				}
	
				System.out.println("Point: " + point);
				System.out.println();
			}
					
			diffCosts.add(cost);
			System.out.println("unsort diffCost: " + diffCosts);
			System.out.println("Cost: " + cost);
			System.out.println("Card: " + card);
			System.out.println("Cards: " + cards);
			System.out.println("RemainingCards: " + remainingCards);
			System.out.println("drawDiscard: " + drawDiscard);
			System.out.println("bestMeldSets: " + bestMeldSets);
			//System.out.println("bestMeldSets size: " + bestMeldSets.get(0).get(0).contains(0));
//			System.out.println("best meld: " + bestMeldSets.get(0).get(0).get(1));
			System.out.println();
			
		}
		
		
		ArrayList<Integer> nstore = new ArrayList<Integer>(diffCosts); // may need to be new ArrayList(nfit)
		Collections.sort(nstore);
		for (int n = 0; n < diffCosts.size(); n++){
		    nstore.add(n, diffCosts.indexOf(nstore.remove(n)));
		}
		Collections.sort(diffCosts);
		
		int max = 0;
		int maxI = -1;
		for (int i = 0; i < diffCosts.size(); i++) {
			if (diffCosts.get(i) > max) { 
				max = diffCosts.get(i);
				maxI = i;
			}
		}
		
		System.out.println("maxI: " + maxI);
		System.out.println("nstore: " + nstore);
		System.out.println("sorted diffCosts: " + diffCosts);
		
		System.out.println("nstore size: " + nstore.size());
		
//		if(nstore.get(nstore.size()-2) == nstore.get(nstore.size()-1)) {
//			if(cards.get(nstore.get(nstore.size()-2)).getRank() > cards.get(nstore.size()-1).getRank()) {
//				System.out.println("Card 9 larger deadwood");
//				candidateCards.clear();
//				candidateCards.add(cards.get(nstore.get(nstore.size()-2)));
//			}
//			else if(cards.get(nstore.size()-1).getRank() > cards.get(nstore.size()-2).getRank()) {
//				System.out.println("Card 10 larger deadwood");
//				candidateCards.clear();
//				candidateCards.add(cards.get(nstore.size()-1));
//			}
//			
//		}
//		else {
			System.out.println("nstore last card: " + nstore.get(nstore.size()-1));
			candidateCards.clear();
			candidateCards.add(cards.get(nstore.get(nstore.size()-1)));
//		}
		
		System.out.println("CandidateCards: " + candidateCards );
		Card discard = candidateCards.get(0);
		// Prevent future repeat of draw, discard pair.
		ArrayList<Card> drawDiscard = new ArrayList<Card>();
		drawDiscard.add(drawnCard);
		drawDiscard.add(discard);
		drawDiscardBitstrings.add(GinRummyUtil.cardsToBitstring(drawDiscard));
		System.out.println("Discard: " + discard);
		return discard;
	}

	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		// Ignore other player discards.  Remove from cards if playerNum is this player.
		if (playerNum == this.playerNum)
			cards.remove(discardedCard);
			discard.push(discardedCard);
	}

	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {
		// Check if deadwood of maximal meld is low enough to go out. 
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), cards) > GinRummyUtil.MAX_DEADWOOD))
			return null;
		return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
	}

	@Override
	public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
		// Melds ignored by simple player, but could affect which melds to make for complex player.
		if (playerNum != this.playerNum)
			opponentKnocked = true;
	}

	@Override
	public void reportScores(int[] scores) {
		// Ignored by simple player, but could affect strategy of more complex player.
	}

	@Override
	public void reportLayoff(int playerNum, Card layoffCard, ArrayList<Card> opponentMeld) {
		// Ignored by simple player, but could affect strategy of more complex player.
		
	}

	@Override
	public void reportFinalHand(int playerNum, ArrayList<Card> hand) {
		// Ignored by simple player, but could affect strategy of more complex player.		
	}
	
}
