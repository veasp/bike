package lv.venta.services;

import lv.venta.models.Inquiry;

import java.util.ArrayList;

public interface IEmailService {

    /**
     * Sends inquiry status changed email to inquiry user
     * @param inquiry Inquiry
     */
    void sendInquiryStatusChangeEmail(Inquiry inquiry);

    /**
     * Sends email of new inquiry to admins
     * @param email List of admin emails
     */
    void sendNewInquiryEmail(ArrayList<String> email);
}
