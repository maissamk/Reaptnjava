package services.gestionCommande;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import java.util.Random;

public class SmsService {

    public static final String ACCOUNT_SID = "AC2edde6b3a8cfb7b57a7c4a3a3181d086";
    public static final String AUTH_TOKEN = "87de261ce9e62d3541040f8a69a934e7";
    public static final String FROM_PHONE = "+12184767418"; // numéro Twilio

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static String envoyerCodePromo(String numero) {
        String code = genererCodePromo();

        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(numero),
                new com.twilio.type.PhoneNumber(FROM_PHONE),
                "Bonjour Mrs/Mm, Votre code promo est : " + code + ". Utilisez-le pour une réduction de 30% !"
        ).create();

        System.out.println("SMS envoyé : " + message.getSid());
        return code;
    }

    private static String genererCodePromo() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(1000000));
    }
}
