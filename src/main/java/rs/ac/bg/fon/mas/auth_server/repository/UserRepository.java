/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package rs.ac.bg.fon.mas.auth_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.bg.fon.mas.auth_server.model.CustomUser;

/**
 *
 * @author Predrag
 */
public interface UserRepository extends JpaRepository<CustomUser, Long>{
    CustomUser findByUsername(String username);
}
