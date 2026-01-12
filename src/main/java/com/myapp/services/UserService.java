package com.myapp.services;



import com.myapp.dao.UserDAO;
import com.myapp.models.User;
import java.util.List;

public class UserService {
    private final UserDAO dao = new UserDAO();

    public List<User> getAllUsers() {
        return dao.findAll();
    }

    public User getUser(int id) {
        return dao.findById(id);
    }

    public boolean addUser(User user) {
        return dao.insert(user);
    }

//    public boolean updateUser(User user) {
//        return dao.update(user);
//    }

//    public boolean deleteUser(int id) {
//        return dao.delete(id);
//    }
}
