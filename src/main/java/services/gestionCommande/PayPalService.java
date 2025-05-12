package services.gestionCommande;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import Models.gestionCommande.Commande;
import Models.gestionCommande.Paiement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PayPalService {
    private static final String CLIENT_ID = "Aa4xlSgNjgz1Hpm5BDJWjrB_UWV-hFlA_Z6tC8MJ22MWK3YSMpKOCsj8CZqOm5wGfK82Y7Mnvq1xXZoB";
    private static final String CLIENT_SECRET = "ECaTz_qZxu8z_s97wDopR7JnqewzF6rGaEBQRlFqnjo-G2GHJcXH4UMyMucM2-smV68ufzQycl032tT_";
    private static final String MODE = "sandbox";

    private APIContext apiContext;
    private static final Logger logger = LoggerFactory.getLogger(PayPalService.class);

    public PayPalService() {
        apiContext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);
    }

    public String createPayment(Commande commande, String returnUrl, String cancelUrl) throws PayPalRESTException {
        try {
            // Validation du montant
            if (commande.getTotale() <= 0) {
                throw new IllegalArgumentException("Le montant de la commande doit être positif");
            }

            // Formatage correct du montant pour PayPal
            BigDecimal amount = new BigDecimal(commande.getTotale()).setScale(2, RoundingMode.HALF_UP);

            // Création du montant PayPal
            Amount paypalAmount = new Amount();
            paypalAmount.setCurrency("DT");
            paypalAmount.setTotal(amount.toString());

            Transaction transaction = new Transaction();
            transaction.setAmount(paypalAmount);
            transaction.setDescription("Commande ReapTN ");

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            Payment payment = new Payment();
            payment.setIntent("sale");
            payment.setPayer(payer);
            payment.setTransactions(transactions);

            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setReturnUrl(returnUrl);
            redirectUrls.setCancelUrl(cancelUrl);
            payment.setRedirectUrls(redirectUrls);

            // Logs pour le débogage
            logger.info("Création d'un paiement PayPal pour la commande #{} d'un montant de {}",
                    commande.getId(), amount.toString());

            Payment createdPayment = payment.create(apiContext);
            logger.info("Paiement créé avec succès pour la commande #{}", commande.getId());

            // Récupération de l'URL d'approbation
            for (Links link : createdPayment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return link.getHref();
                }
            }

            throw new PayPalRESTException("URL d'approbation non trouvée dans la réponse PayPal");
        } catch (PayPalRESTException e) {
            logger.error("Erreur lors de la création du paiement pour la commande #{}: {}",
                    commande.getId(), e.getMessage());
            throw e;
        }
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        try {
            Payment payment = new Payment();
            payment.setId(paymentId);

            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            Payment executedPayment = payment.execute(apiContext, paymentExecution);
            logger.info("Paiement exécuté avec succès (ID: {})", paymentId);

            return executedPayment;
        } catch (PayPalRESTException e) {
            logger.error("Erreur lors de l'exécution du paiement (ID: {}): {}",
                    paymentId, e.getMessage());
            throw e;
        }
    }

    public Paiement enregistrerPaiement(Commande commande, String paymentId) {
        try {
            Paiement paiement = new Paiement();
            paiement.setCommande(commande);
            paiement.setDatePaiement(new Date());
            paiement.setMethodePaiement("PayPal (ID: " + paymentId + ")");

            PaiementService paiementService = new PaiementService();
            paiementService.ajouter(paiement);
            logger.info("Paiement enregistré avec succès pour la commande #{}", commande.getId());

            return paiement;
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement du paiement pour la commande #{}: {}",
                    commande.getId(), e.getMessage());
            throw e;
        }
    }
}