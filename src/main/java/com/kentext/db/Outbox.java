package com.kentext.db;

/**
 *
 * @author arthur
 */
import com.kentext.common.Common;
import static com.kentext.common.Common.LOGGER;
import static com.kentext.common.Common.SCHEDULED;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.swing.JOptionPane;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "outbox")
public class Outbox implements Serializable, Common
{
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "origin", nullable = false, columnDefinition = "TEXT")
    private String origin;

    @Column(name = "destination", nullable = false, columnDefinition = "TEXT")
    private String destination;

    @Column(name = "send_date", nullable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private String send_date;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent", nullable = false)
    @ColumnDefault("0")
    private String sent;

    @Column(name = "is_token", nullable = false)
    @ColumnDefault("0")
    private int is_token;

    @Column(name = "sent_by", nullable = false)
    @ColumnDefault("0")
    private String sent_by;

    @Column(name = "status", nullable = false, columnDefinition = "TEXT")
    @ColumnDefault("0")
    private String status;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getOrigin()
    {
        return origin;
    }

    public void setOrigin(String origin)
    {
        this.origin = origin;
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public String getSend_date()
    {
        return send_date;
    }

    public void setSend_date(String send_date)
    {
        this.send_date = send_date;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getSent()
    {
        return sent;
    }

    public void setSent(String sent)
    {
        this.sent = sent;
    }

    public int getIs_token()
    {
        return is_token;
    }

    public void setIs_token(int is_token)
    {
        this.is_token = is_token;
    }

    public String getSent_by()
    {
        return sent_by;
    }

    public void setSent_by(String sent_by)
    {
        this.sent_by = sent_by;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * Store an SMS message to database
     *
     * @param origin From whom the message originates
     * @param destination The destination of the message
     * @param message The message itself, encrypted
     * @param sent Flag denoting whether the message has been sent or not
     * @param status The status of the sending
     * @param sendDate The date when the message is to be sent
     * @param isToken Flags if this message is the token used to authenticate a
     * user
     * @param sentBy The sender id
     *
     * @return TRUE if message successfully stored, FALSE otherwise
     */
    public boolean saveMessage(String origin, String destination, String message, String sent, String status, String sendDate, int isToken, String sentBy)
    {
        boolean saved = false;

        // Create an EntityManager
        EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        try
        {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            // Create a new Student object
            Outbox outbox = new Outbox();
            outbox.setOrigin(origin);
            outbox.setDestination(destination);
            outbox.setMessage(message);
            outbox.setSent(sent);
            outbox.setStatus(status);
            outbox.setSend_date(sendDate);
            outbox.setIs_token(isToken);
            outbox.setSent_by(sentBy);
            outbox.setId(UUID.randomUUID().toString());

            // Save the student object
            manager.persist(outbox);

            // Commit the transaction
            transaction.commit();

            saved = true;
        }
        catch (Exception ex)
        {
            // If there are any exceptions, roll back the changes
            if (transaction != null)
            {
                transaction.rollback();
            }

            // Print the Exception
            LOGGER.severe(ex.getMessage());
        }
        finally
        {
            // Close the EntityManager
            manager.close();
        }

        return saved;
    }

    /**
     * Read all the messages.
     *
     * @return a List of Students
     */
    private List<Outbox> readAllMessages()
    {
        List<Outbox> messages = null;

        // Create an EntityManager
        EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        try
        {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            // Get a List of Students
            messages = manager.createQuery(
                    "SELECT o FROM Outbox o",
                    com.kentext.db.Outbox.class
            ).getResultList();

            // Commit the transaction
            transaction.commit();
        }
        catch (Exception ex)
        {
            // If there are any exceptions, roll back the changes
            if (transaction != null)
            {
                transaction.rollback();
            }
            // Print the Exception
            LOGGER.severe(ex.getMessage());
        }
        finally
        {
            // Close the EntityManager
            manager.close();
        }

        return messages;
    }

    /**
     * Delete the existing Student.
     *
     * @param id
     */
    public void deleteMessage(String id)
    {
        // Create an EntityManager
        EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        try
        {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            // Get the Student object
            Outbox outbox = manager.find(Outbox.class, id);

            // Delete the student
            manager.remove(outbox);

            // Commit the transaction
            transaction.commit();
        }
        catch (Exception ex)
        {
            // If there are any exceptions, roll back the changes
            if (transaction != null)
            {
                transaction.rollback();
            }
            // Print the Exception
            LOGGER.severe(ex.getMessage());
        }
        finally
        {
            // Close the EntityManager
            manager.close();
        }
    }

    @PrePersist
    public void prePersist() throws GeneralSecurityException
    {
        if (send_date == null) //We set default value in case if the value is not set yet.
        {
            send_date = DATE_FORMATTER.format(LocalDateTime.now());
        }
    }

    public HashMap<String, HashMap<String, String>> getMessagesSentBy(String origin)
    {
        List<Outbox> messages = readAllMessages();

        HashMap<String, HashMap<String, String>> myMessages = new HashMap();

        for (Outbox outboxMessage : messages)
        {
            if (outboxMessage.getSent_by().equals(origin))
            {
                HashMap<String, String> messageParameters = new HashMap();

                messageParameters.put("id", outboxMessage.getId());
                messageParameters.put("origin", outboxMessage.getOrigin());
                messageParameters.put("destination", outboxMessage.getDestination());
                messageParameters.put("send_date", outboxMessage.getSend_date());
                messageParameters.put("message", outboxMessage.getMessage());
                messageParameters.put("sent", String.valueOf(outboxMessage.getSent()));
                messageParameters.put("is_token", String.valueOf(outboxMessage.getIs_token()));
                messageParameters.put("sent_by", outboxMessage.getSent_by());
                messageParameters.put("status", String.valueOf(outboxMessage.getStatus()));

                myMessages.put(outboxMessage.getId(), messageParameters);
            }
        }

        return myMessages;
    }

    public HashMap<String, HashMap<String, String>> getPendingMessages()
    {
        List<Outbox> messages = readAllMessages();

        HashMap<String, HashMap<String, String>> myMessages = new HashMap();

        for (Outbox outboxMessage : messages)
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            try
            {
            
            if (
                outboxMessage.getSent().equals(String.valueOf(SCHEDULED)) &&
                    simpleDateFormat.parse(outboxMessage.getSend_date())
                            .before(simpleDateFormat.parse(DATE_FORMATTER.format(LocalDateTime.now())))
            )
            {

                HashMap<String, String> messageParameters = new HashMap();

                messageParameters.put("id", outboxMessage.getId());
                messageParameters.put("origin", outboxMessage.getOrigin());
                messageParameters.put("destination", outboxMessage.getDestination());
                messageParameters.put("send_date", outboxMessage.getSend_date());
                messageParameters.put("message", outboxMessage.getMessage());
                messageParameters.put("sent", String.valueOf(outboxMessage.getSent()));
                messageParameters.put("is_token", String.valueOf(outboxMessage.getIs_token()));
                messageParameters.put("sent_by", outboxMessage.getSent_by());
                messageParameters.put("status", String.valueOf(outboxMessage.getStatus()));

                myMessages.put(outboxMessage.getId(), messageParameters);
            }
            }
            catch (ParseException ex)
            {
                LOGGER.severe(ex.getMessage());
            }
        }
        
        return myMessages;
    }

    public void setSendStatus(String id, String status, String errorMessage)
    {
        // Create an EntityManager
        EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        try
        {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            // Get a new Outbox object
            Outbox outbox = manager.find(Outbox.class, id);
            
            outbox.setSent(status);
            outbox.setStatus(errorMessage == null ? "" : errorMessage);
            
            manager.persist(outbox);

            // Commit the transaction
            transaction.commit();
        }
        catch (Exception ex)
        {
            // If there are any exceptions, roll back the changes
            if (transaction != null)
            {
                transaction.rollback();
            }
            // Print the Exception
            LOGGER.severe(ex.getMessage());
        }
        finally
        {
            // Close the EntityManager
            manager.close();
        }
    }
}
