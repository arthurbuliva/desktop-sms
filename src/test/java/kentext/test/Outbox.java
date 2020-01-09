package kentext.test;

/**
 *
 * @author arthur
 */
import com.kentext.common.Common;
import com.kentext.security.Enigma;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Query;
import javax.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "outbox")
public class Outbox implements Serializable, Common
{
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private int id;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "send_date", nullable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private String send_date;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent", nullable = false)
    @ColumnDefault("0")
    private int sent;

    @Column(name = "is_token", nullable = false)
    @ColumnDefault("0")
    private int is_token;

    @Column(name = "sent_by", nullable = false)
    @ColumnDefault("0")
    private int sent_by;

    @Column(name = "status", nullable = false, columnDefinition = "TEXT")
    @ColumnDefault("0")
    private int status;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
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

    public int getSent()
    {
        return sent;
    }

    public void setSent(int sent)
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

    public int getSent_by()
    {
        return sent_by;
    }

    public void setSent_by(int sent_by)
    {
        this.sent_by = sent_by;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    /**
     * Create a new Student.
     *
     * @param id
     */
    public void create(int id)
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

            // Create a new Student object
            Outbox outbox = new Outbox();
            outbox.setId(id);
            outbox.setOrigin("254720000000");
            outbox.setDestination("25472213456");
            outbox.setMessage("Hello world");

            // Save the student object
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

    /**
     * Read all the Students.
     *
     * @return a List of Students
     */
    public List readAll()
    {

        List<Outbox> students = null;

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
            students = manager.createQuery(
                "SELECT o FROM Outbox o",
                kentext.test.Outbox.class
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
        return students;
    }

    /**
     * Delete the existing Student.
     *
     * @param id
     */
    public void delete(int id)
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
            Outbox stu = manager.find(Outbox.class, id);

            // Delete the student
            manager.remove(stu);

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

    /**
     * Update the existing Student.
     *
     * @param id
     */
    public void upate(int id)
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

            // Change the values
//            outbox.setName(name);
//            stu.setAge(age);
            // Update the student
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

    @PrePersist
    public void prePersist() throws GeneralSecurityException
    {
        if (send_date == null) //We set default value in case if the value is not set yet.
        {
            send_date = DATE_FORMATTER.format(LocalDateTime.now());
        }
        
        try
        {
            Enigma enigma = new Enigma();
            message = enigma.encryptText(message);
        }
        catch (Exception e)
        {
            LOGGER.severe(e.getMessage());
            
            throw new java.security.GeneralSecurityException();
        }
    }
}
