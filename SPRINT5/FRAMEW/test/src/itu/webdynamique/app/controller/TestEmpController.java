// package itu.webdynamique.app.controller;

// import itu.webdynamique.framework.ModelAndView;

// import java.util.List;
// import java.util.Map;

// public class TestEmpController {

//     public static void main(String[] args) {
//         EmpController controller = new EmpController();
//         ModelAndView mv = controller.afficherListe();

//         String url = mv.getUrl();
//         Map<String, Object> data = mv.getData();

//         System.out.println("URL attendue : " + url);
//         System.out.println("Titre attendu : " + data.get("titre"));
//         System.out.println("Employés attendus : " + data.get("employes"));

//         if (!"employe/liste".equals(url)) {
//             throw new AssertionError("URL incorrecte : " + url);
//         }

//         if (!"Liste des Employes".equals(data.get("titre"))) {
//             throw new AssertionError("Titre incorrect : " + data.get("titre"));
//         }

//         Object employes = data.get("employes");
//         if (!(employes instanceof List)) {
//             throw new AssertionError("Attribut employes non trouvé ou mauvais type : " + employes);
//         }

//         @SuppressWarnings("unchecked")
//         List<String> listeEmp = (List<String>) employes;
//         if (listeEmp.size() != 3 || !listeEmp.contains("Mino") || !listeEmp.contains("Nanou") || !listeEmp.contains("Alime")) {
//             throw new AssertionError("Liste des employés incorrecte : " + listeEmp);
//         }

//         System.out.println("TestEmpController réussi.");
//     }
// }
