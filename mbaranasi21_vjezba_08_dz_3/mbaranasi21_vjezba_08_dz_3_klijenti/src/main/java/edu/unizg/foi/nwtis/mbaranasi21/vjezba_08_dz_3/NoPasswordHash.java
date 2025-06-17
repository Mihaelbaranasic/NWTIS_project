package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;


import java.util.Map;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

public class NoPasswordHash implements Pbkdf2PasswordHash {

  @Override
  public String generate(char[] password) {
    return password.toString();
  }

  @Override
  public boolean verify(char[] password, String hashedPassword) {
    var npassword = new String(password);
    if (npassword.trim().compareTo(hashedPassword.trim()) == 0) {
      return true;
    }
    return false;
  }

  @Override
  public void initialize(Map<String, String> parameters) {}

}
