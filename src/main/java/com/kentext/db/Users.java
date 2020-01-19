package com.kentext.db;

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
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "Users")
public class Users implements Serializable, Common
{
    @Id
    @Column(name = "id")
    private String id;

    // TODO: This unique constraint is not working
    @Column(name = "username", nullable = false, unique = true, columnDefinition = "TEXT")
    private String username;

    @Column(name = "password", nullable = false, columnDefinition = "TEXT")
    private String password;

    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;
    
    @Column(name = "token_active_until", nullable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private String token_active_until;

    @Column(name = "active", nullable = false)
    @ColumnDefault("0")
    private int active;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getToken_active_until()
    {
        return token_active_until;
    }

    public void setToken_active_until(String token_active_until)
    {
        this.token_active_until = token_active_until;
    }

    public int getActive()
    {
        return active;
    }

    public void setActive(int active)
    {
        this.active = active;
    }
    
    
   
    /**
     * Store an SMS token to database
     * 
     * @param username
     * @param password
     * @param token
     * @param token_active_until
     * @param active
     * 
     * @return TRUE if token successfully stored, FALSE otherwise 
     */
    public boolean createNewUser(String username, String password, String token, String token_active_until, int active)
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
            
            Enigma enigma = new Enigma();

            // Create a new Student object
            Users users = new Users();
            
            users.setId(UUID.randomUUID().toString());
            users.setUsername(username);
            users.setPassword(enigma.encryptText(password));
            users.setToken(token);
            users.setToken_active_until(token_active_until);
            users.setActive(active);
            

            // Save the student object
            manager.persist(users);

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
            
            ex.printStackTrace();
        }
        finally
        {
            // Close the EntityManager
            manager.close();
        }
        
        return saved;
    }

    /**
     * Read all the Students.
     *
     * @return a List of Students
     */
    public static List<Users> listAllUsers()
    {
        List<Users> students = null;

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
            students = manager.createQuery("SELECT u FROM Users u",
                com.kentext.db.Users.class
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
    public void deleteUser(String id)
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
            Users stu = manager.find(Users.class, id);

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
     * Activate a given user
     * 
     * @param username The user in which we are interested
     * 
     * @return TRUE if successfully activated, FALSE otherwise.
     */
    public boolean activateUser(String username)
    {
        // Create an EntityManager
        EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = null;
        
        boolean activated = false;
        
        String userIdToActivate = retrieveUser(username).getId();

        try
        {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            // Get the Student object
            Users user = manager.find(Users.class, userIdToActivate);

            // Change the values
            user.setActive(active);
            manager.persist(user);

            // Commit the transaction
            transaction.commit();
            
            activated = true;
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
            
            return false;
        }
        finally
        {
            // Close the EntityManager
            manager.close();
            
            return activated;
        }
    }

    @PrePersist
    public void prePersist() throws GeneralSecurityException
    {
        if (token_active_until == null) //We set default value in case if the value is not set yet.
        {
            token_active_until = DATE_FORMATTER.format(LocalDateTime.now());
        }
    }

    public Users retrieveUser(String username)
    {
        List<Users> users = listAllUsers();
        
        for (Users user : users)
        {
            if(user.getUsername().equals(username))
            {
                return user;
            }
        }
        
        return null;
    }
}
