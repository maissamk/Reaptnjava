package services.gestionCommande;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import java.util.Random;

public class SmsService {

    public static final String ACCOUNT_SID = "";
    public static final String AUTH_TOKEN = "";
    public static final String FROM_PHONE = "+"; // numéro Twilio

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static String envoyerCodePromo(String numero) {
        String code = genererCodePromo();

        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(numero),
                new com.twilio.type.PhoneNumber(FROM_PHONE),
                "Votre code promo est : " + code + ". Utilisez-le pour une réduction de 30% !"
        ).create();

        System.out.println("SMS envoyé : " + message.getSid());
        return code;
    }

    private static String genererCodePromo() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(1000000));
    }
}
