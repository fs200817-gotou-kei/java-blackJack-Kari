import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;

/**
 * BlackJack
 */
public class Blackjack {

    private static final int NUMBER_OF_PLAYER = 2;

    private static final int FIRST_DEAL_CARD_MAX_COUNT = 2;

    private static final int PLAYER_INDEX = 0;
    private static final int DEALER_INDEX = 1;

    private static final String[] NAMES = { "あなた", "ディーラー" };

    private static final int BLACKJACK_SCORE = 21;
    private static final int BLACKJACK_CARD_COUNT = 2;

    private static final int DEALER_LIMIT_SCORE = 17;

    private static final String DEAL_CARD_SECRET = "?";

    private static final int FIRST_SET_COIN = 100;

    private static final int ONE_PLAY_BET_COIN = 10;
    private static final int BLACKJACK_RETURN_COIN = 30;
    private static final int WIN_RETURN_COIN = 20;
    private static final int DRAW_RETURN_COIN = 10;

    private static final Map<String, Boolean> WHETHER_TO_HIT_OR_STAND = new HashMap<String, Boolean>() {
        {
            put("Y", true);
            put("N", false);
        }
    };

    private static final Scanner STDIN = new Scanner(System.in);
    private static final Random RANDOM = new Random();

    private enum CardRankTypes {

        ACE("A", 1), TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5),
        SIX("6", 6), SEVEN("7", 7), EIGHT("8", 8), NINE("9", 9), TEN("10", 10),
        JACK("J", 10), QUEEN("Q", 10), KING("K", 10),;

        private String cardName;
        private int cardNumber;
        private final static int ACE_TO_ELEVEN = 11;

        private CardRankTypes(String cardName, int cardNumber) {

            this.cardName = cardName;
            this.cardNumber = cardNumber;
        }

        public String getCardName() {
            return this.cardName;
        }

        private int parseAceToEleven() {
            return ACE_TO_ELEVEN;
        }

        private int getCardNumber() {
            return this.cardNumber;
        }

        //TODO: 10が均等にしか持ってこれない
        public static CardRankTypes of(int cardNumber) {
            return Arrays.stream(Blackjack.CardRankTypes.values())
                    .filter(e -> cardNumber == e.getCardNumber()).findFirst()
                    .orElse(CardRankTypes.ACE);
        }

        public static boolean isAce(Blackjack.CardRankTypes cardName) {
            return cardName.getCardName() == ACE.getCardName();
        }
    }

    public static void main(String[] args) {

        int stockedCoins = FIRST_SET_COIN;

        while (hasBetCoin(stockedCoins)) {

            stockedCoins = betCoin(stockedCoins);

            List<List<CardRankTypes>> handCards = new ArrayList<>();
            int[] scores = new int[NUMBER_OF_PLAYER];

            gameStartSetting(handCards, scores);

            operateByPlayer(handCards, scores);

            operateByDealer(handCards, scores);

            stockedCoins = finalResultJudge(handCards, scores, stockedCoins);
        }
    }

    private static int betCoin(int stockedCoin) {
        return stockedCoin - ONE_PLAY_BET_COIN;
    }

    private static boolean hasBetCoin(int stockedCoin) {
        return stockedCoin >= ONE_PLAY_BET_COIN;
    }

    private static void operateByDealer(List<List<CardRankTypes>> handCards,
            int[] scores) {

        if (!isWhetherOrBusted(scores) && !isBlackJack(handCards, scores)) {

            while (!isOverDealerStopScore(scores)) {

                dealCard(handCards.get(DEALER_INDEX), DEALER_INDEX);
                updateScore(handCards.get(DEALER_INDEX), scores, DEALER_INDEX);
            }
        }
    }

    private static void operateByPlayer(List<List<CardRankTypes>> handCards,
            int[] scores) {

        while (!isWhetherOrBusted(scores) && !isBlackJack(handCards, scores)) {

            if (!isReceiveWhetherToHit())
                return;

            dealCard(handCards.get(PLAYER_INDEX), PLAYER_INDEX);
            updateScore(handCards.get(PLAYER_INDEX), scores, PLAYER_INDEX);
        }
    }

    //TODO: 何したいのかわからない
    private static boolean isReceiveWhetherToHit() {

        String inputWhetherToHitAnswer = "";
        boolean isValidatedWhetherToHitAnswer = true;

        showWhetherListenToHitMessage();

        while (isValidatedWhetherToHitAnswer) {

            inputWhetherToHitAnswer = STDIN.nextLine();

            isValidatedWhetherToHitAnswer = !isValidatedWhetherToHitAnswer(
                    inputWhetherToHitAnswer);
        }

        return isWhetherToHitAnswer(inputWhetherToHitAnswer);
    }

    private static boolean isWhetherToHitAnswer(
            String inputWhetherToHitAnswer) {

        return WHETHER_TO_HIT_OR_STAND.get(inputWhetherToHitAnswer);
    }

    private static void showWhetherListenToHitMessage() {
        System.out.print("もう一枚カードを引きますか？Y/N:");
    }

    private static boolean isValidatedWhetherToHitAnswer(
            String inputWhetherToHitAnswer) {

        if (isContainsHitAnswer(inputWhetherToHitAnswer))
            return true;

        showHitAnswerOfErrorMessage();

        return false;
    }

    private static boolean isContainsHitAnswer(String standAnswer) {

        return WHETHER_TO_HIT_OR_STAND.containsKey(standAnswer);
    }

    private static void showHitAnswerOfErrorMessage() {
        System.out.println("YかNを入力してください");
    }

    private static int finalResultJudge(List<List<CardRankTypes>> handCards,
            int[] scores, int stockedCoins) {

        if (isWin(scores))
            stockedCoins = resultCaseAtWin(scores, handCards, stockedCoins);

        if (isLose(scores))
            resultCaseAtLose(scores, handCards, stockedCoins);

        if (isDraw(scores))
            resultCaseAtDraw(scores, handCards, stockedCoins);

        return stockedCoins;
    }

    private static void resultCaseAtDraw(int[] scores,
            List<List<Blackjack.CardRankTypes>> handCards, int stockedCoins) {

        if (isBlackJack(handCards, scores)) {
            stockedCoins = drawCaseAtBlackJack(stockedCoins);
            return;
        }

        if (isBusted(scores[PLAYER_INDEX]) && isBusted(scores[DEALER_INDEX])) {
            drawCaseAtBusted();
            return;
        }

        drawCaseAtNomal();
    }

    private static void drawCaseAtNomal() {
        showDrawMessage();
    }

    private static void drawCaseAtBusted() {
        showBustedMessage();
        showDrawMessage();
    }

    private static int drawCaseAtBlackJack(int stockedCoins) {
        showBlackJackMessage();
        stockedCoins = returnCoinAtDraw(stockedCoins);
        showDrawMessage();
        showStockedCoins(stockedCoins);
        return stockedCoins;
    }

    private static void resultCaseAtLose(int[] scores,
            List<List<Blackjack.CardRankTypes>> handCards, int stockedCoins) {

        if (isBlackJack(handCards, scores)) {
            loseCaseAtBlackJack(stockedCoins);
            return;
        }

        if (isBusted(scores[PLAYER_INDEX])) {
            loseCaseAtBusted();
            return;
        }
        loseCaseAtNomal();
    }

    private static void loseCaseAtBusted() {
        showBustedMessage();
        showLoseMessage();
    }

    private static void loseCaseAtNomal() {
        showLoseMessage();
    }

    private static void loseCaseAtBlackJack(int stockedCoins) {
        showBlackJackMessage();
        showLoseMessage();
    }

    private static int resultCaseAtWin(int[] scores,
            List<List<Blackjack.CardRankTypes>> handCards, int stockedCoins) {

        if (isBlackJack(handCards, scores)) {
            stockedCoins = winCaseAtBlackJack(stockedCoins);
            return stockedCoins;
        }

        if (isBusted(scores[DEALER_INDEX])) {
            stockedCoins = winCaseAtBusted(stockedCoins);
            return stockedCoins;
        }

        stockedCoins = winCaseAtNomal(stockedCoins);
        return stockedCoins;
    }

    private static int winCaseAtNomal(int stockedCoins) {
        stockedCoins = returnCoinAtWin(stockedCoins);
        showWinMessage();
        return stockedCoins;
    }

    private static int winCaseAtBusted(int stockedCoins) {
        stockedCoins = returnCoinAtWin(stockedCoins);
        showBustedMessage();
        showWinMessage();
        return stockedCoins;
    }

    private static int winCaseAtBlackJack(int stockedCoins) {
        showBlackJackMessage();
        stockedCoins = returnCoinAtBlackJack(stockedCoins);
        showWinMessage();
        return stockedCoins;
    }

    private static void showStockedCoins(int stockedCoins) {
        System.out.printf("勝負の結果残りのコインは%dだよ %n", stockedCoins);
    }

    private static int returnCoinAtDraw(int stockedCoins) {
        return stockedCoins + DRAW_RETURN_COIN;
    }

    private static int returnCoinAtWin(int stockedCoins) {
        return stockedCoins + WIN_RETURN_COIN;
    }

    private static int returnCoinAtBlackJack(int stockedCoins) {
        return stockedCoins + BLACKJACK_RETURN_COIN;
    }

    private static void showBlackJackMessage() {
        System.out.println("ブラックジャック!!");
    }

    //TODO: 条件式ながい
    private static boolean isBlackJack(
            List<List<Blackjack.CardRankTypes>> handCards, int[] scores) {

        return (isBlackJackCombi(scores[DEALER_INDEX])
                && isHandCardsCountForBlackJack(handCards.get(DEALER_INDEX))
                || (isBlackJackCombi(scores[PLAYER_INDEX])
                        && isHandCardsCountForBlackJack(
                                handCards.get(PLAYER_INDEX))));
    }

    private static boolean isHandCardsCountForBlackJack(
            List<Blackjack.CardRankTypes> handCards) {
        return handCards.size() == BLACKJACK_CARD_COUNT;
    }

    private static boolean isBlackJackCombi(int score) {
        return score == BLACKJACK_SCORE;
    }

    private static void showBustedMessage() {
        System.out.print("バースト!!");
    }

    private static void showDrawMessage() {
        System.out.println("引き分けです。");
        System.out.println();
    }

    private static boolean isDraw(int[] scores) {
        return scores[PLAYER_INDEX] == scores[DEALER_INDEX];
    }

    private static boolean isLose(int[] scores) {
        return isLoseScore(scores) && !isBusted(scores[DEALER_INDEX])
                || isBusted(scores[PLAYER_INDEX])
                        && !isBusted(scores[DEALER_INDEX]);
    }

    private static boolean isLoseScore(int[] scores) {
        return scores[PLAYER_INDEX] < scores[DEALER_INDEX];
    }

    private static void showLoseMessage() {
        System.out.println("あなたの負けです。");
        System.out.println();
    }

    private static void showWinMessage() {
        System.out.println("あなたの勝ちです。");
        System.out.println();
    }

    private static boolean isWin(int[] scores) {
        return (isWinScore(scores) && !isBusted(scores[PLAYER_INDEX]))
                || (!isBusted(scores[PLAYER_INDEX])
                        && isBusted(scores[DEALER_INDEX]));
    }

    private static boolean isWinScore(int[] scores) {
        return scores[PLAYER_INDEX] > scores[DEALER_INDEX];
    }

    private static boolean isOverDealerStopScore(int[] scores) {
        return scores[DEALER_INDEX] > DEALER_LIMIT_SCORE;
    }

    private static boolean isWhetherOrBusted(int[] scores) {
        return isBusted(scores[PLAYER_INDEX]) || isBusted(scores[DEALER_INDEX]);
    }

    private static boolean isBusted(int score) {
        return score > BLACKJACK_SCORE;
    }

    private static void gameStartSetting(List<List<CardRankTypes>> handCards,
            int[] scores) {

        dealCardsAtFirst(handCards);

        initScoresAtFirst(handCards, scores);
    }

    private static void initScoresAtFirst(List<List<CardRankTypes>> handCards,
            int[] scores) {

        for (int i = 0; i < NUMBER_OF_PLAYER; i++) {
            scores[i] = calcScore(handCards.get(i));
        }

        showScore(scores, PLAYER_INDEX);
    }

    private static void updateScore(List<CardRankTypes> handCards, int[] scores,
            int playerIndex) {

        scores[playerIndex] = calcScore(handCards);

        showScore(scores, playerIndex);
    }

    private static void showScore(int[] scores, int playerIndex) {
        System.out.printf("%sの合計は%dです%n", NAMES[playerIndex],
                scores[playerIndex]);
    }

    private static int calcScore(List<CardRankTypes> handCards) {

        int score = 0;
        List<Integer> handCardsNum;

        handCardsNum = generateCardsNameToInteger(handCards);
        score = sumScore(handCardsNum);

        while (isBusted(score)) {

            if (!hasAce(handCardsNum))
                break;

            calcScoreIfAceCard(handCardsNum);
            score = sumScore(handCardsNum);
        }

        return score;
    }

    //TODO: addメソッド化とか構造確認
    private static List<Integer> generateCardsNameToInteger(
            List<Blackjack.CardRankTypes> handCards) {

        List<Integer> handCardsNum = new ArrayList<>();

        for (int i = 0; i < handCards.size(); i++) {

            if (CardRankTypes.isAce(handCards.get(i))) {
                handCardsNum.add(handCards.get(i).parseAceToEleven());
                continue;
            }

            handCardsNum.add(handCards.get(i).getCardNumber());
        }

        return handCardsNum;
    }

    private static void calcScoreIfAceCard(List<Integer> handCardsNum) {

        int aceIndex = seachAceIndexInHandCards(handCardsNum);

        handCardsNum.set(aceIndex, CardRankTypes.ACE.getCardNumber());
    }

    private static int seachAceIndexInHandCards(List<Integer> handCardsNum) {
        return handCardsNum.indexOf(CardRankTypes.ACE_TO_ELEVEN);
    }

    //TODO: enum側のisAceとややこしい
    private static boolean hasAce(List<Integer> handCardsNum) {
        return handCardsNum.contains(CardRankTypes.ACE_TO_ELEVEN);
    }

    private static int sumScore(List<Integer> handCardsNum) {

        int score = 0;

        for (int i = 0; i < handCardsNum.size(); i++) {
            score += handCardsNum.get(i);
        }

        return score;
    }

    //TODO: 冗長
    private static void dealCardsAtFirst(List<List<CardRankTypes>> handCards) {

        handCards.add(new ArrayList<>());
        handCards.add(new ArrayList<>());

        for (int i = 1; i <= FIRST_DEAL_CARD_MAX_COUNT; i++) {

            CardRankTypes playerDrawCard = drawCard(CardRankTypes.values());
            showDrawCard(playerDrawCard.getCardName(), NAMES[PLAYER_INDEX]);
            appendDrawCardToHand(playerDrawCard, handCards.get(PLAYER_INDEX));

            CardRankTypes dealerDrawCard = drawCard(CardRankTypes.values());
            if (isDealOfLastTime(i)) {
                showDrawCard(DEAL_CARD_SECRET, NAMES[DEALER_INDEX]);
                appendDrawCardToHand(dealerDrawCard,
                        handCards.get(DEALER_INDEX));
                return;
            }

            showDrawCard(dealerDrawCard.getCardName(), NAMES[DEALER_INDEX]);
            appendDrawCardToHand(dealerDrawCard, handCards.get(DEALER_INDEX));
        }
    }

    private static boolean isDealOfLastTime(int dealCardCount) {
        return dealCardCount == FIRST_DEAL_CARD_MAX_COUNT;
    }

    private static void dealCard(List<CardRankTypes> handCards,
            int playerIndex) {

        CardRankTypes drawCard = drawCard(CardRankTypes.values());

        showDrawCard(drawCard.getCardName(), NAMES[playerIndex]);

        appendDrawCardToHand(drawCard, handCards);
    }

    private static void appendDrawCardToHand(CardRankTypes drawCard,
            List<CardRankTypes> handCards) {

        handCards.add(drawCard);
    }

    private static void showDrawCard(String drawCard, String name) {
        System.out.printf("%sに「%s」が配られました%n", name, drawCard);
    }

    private static CardRankTypes drawCard(CardRankTypes[] allCards) {

        int drawCardNum = RANDOM.nextInt(allCards.length);

        CardRankTypes drawCardName = CardRankTypes.of(drawCardNum);

        return drawCardName;
    }
}
