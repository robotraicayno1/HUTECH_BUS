package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "TicketPasses")
public class TicketPass {

    @Id
    private String id; // Format: PASS-123456

    // username of the student owning this pass
    private String username;

    // Type of pass: WEEK, MONTH, YEAR
    private String type;

    // The route this pass applies to
    private String route;

    // Expected valid period
    private Date startDate;
    private Date expiryDate;
    private Date purchaseDate;

    // 'ACTIVE', 'EXPIRED', 'PENDING'
    private String status;
}
