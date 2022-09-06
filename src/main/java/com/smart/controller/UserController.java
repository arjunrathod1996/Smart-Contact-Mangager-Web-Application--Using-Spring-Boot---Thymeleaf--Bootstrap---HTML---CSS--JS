package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	/* method for adding common data to response*/

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		
		System.out.println("username " +userName);
		
		/* Get the user using username(Email) */
		
		User user =  userRepository.getUserByUserName(userName);
		
		System.out.println(user);
		
		model.addAttribute("user", user);
	}
	
	
	/*dashboard home*/
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title", "User Dashboard");
		
		return "normal/user_dashboard";
	}
	
	
	
	
	/* Open add form handler */
	
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add Contact Page");
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	
	/*processing add contact form*/
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file ,Principal principal,HttpSession session) {
		
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
//		if(3>2) {
//			throw new Exception();
//		}
		
		/*procession and uploading file...*/
		
		if(file.isEmpty()) {
			/* if the file is empty then try our message*/
			
			System.out.println("File is empty");
			
			contact.setImage("contact.png");
		}else {
			/* file the file to folder and update the name to contact */
		
			contact.setImage(file.getOriginalFilename());
			
			
			File saveFile = new ClassPathResource("static/img").getFile();
		
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is Uploaded");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		
		System.out.println("Data "+ contact);
		System.out.println("Added to  data base");
		
		/* Message Success */
		
		session.setAttribute("message", new Message("Your contact is add !! and add more..","success"));
		
		
		
		
		}catch(Exception e) {
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			
			/* Message Error */
			
			session.setAttribute("message", new Message("Something Went Wrong !! try again.. ","danger"));
		}
		return"normal/add_contact_form";
	}
	
	
	/* show contacts handler */
	
	
	
	
	/* Per page = 5[n]*/
	/* current page = 0 [page]*/
	

	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m,Principal principal) {
		
		m.addAttribute("title", "Show User Contacts Page");
		
		/* contact ki list ko bhejini hai*/
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		
		/* cuurent page*/
		/* contact per page -5*/
		
		Pageable pageable = PageRequest.of(page, 3);
		
		 Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		 m.addAttribute("contacts", contacts);
		 
		 m.addAttribute("currentPage", page);
		
		 m.addAttribute("totalPages",contacts.getTotalPages());
		 
		return "normal/show_contacts";
	}
	
	/* Showing particular contact details*/
	
	@RequestMapping("/{Cid}/contact")
	public String showContactDetails(@PathVariable("Cid") Integer Cid, Model model,Principal principal) {
		System.out.println("Cid"  +Cid);
		
		Optional<Contact> contactOptional =  this.contactRepository.findById(Cid);
		
		Contact contact =  contactOptional.get();
		
		
		/**/
		String userName =  principal.getName();
		
		User user =  this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			model.addAttribute("title", contact.getName());
		}
		
		
		
		
		return "normal/contact_detail";
	}
	
	/* delete contact handler*/
	
	@GetMapping("/delete/{Cid}")
	public String deleteContact(@PathVariable("Cid") Integer Cid, Model model,HttpSession session,Principal principal) {
		
		System.out.println("cid : " + Cid);
		
		Contact contact = this.contactRepository.findById(Cid).get();
		
		 
		
		 
		 System.out.println("contact :" + contact.getCid());
		 
//		 contact.setUser(null);
		 
//		this.contactRepository.delete(contact);
		 
		 User user = this.userRepository.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		 
		 System.out.println("deleted");
		
		session.setAttribute("message", new Message("contact deleted succesfully...","success"));
		return "redirect:/user/show-contacts/0";
	}
	
	/* open update form handler*/
	@PostMapping("/update-contact/{Cid}")
	public String updateForm(@PathVariable("Cid") Integer Cid, Model m) {
		
		m.addAttribute("title","Update Contact");
		
	   Contact contact = this.contactRepository.findById(Cid).get();
	   
	   m.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	/* update contact handler */
	/* */
	@RequestMapping(value= "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file , Model m, HttpSession session, Principal principal) {
		
		try {
			
			/* old contact details*/
			
			Contact oldContactDetails = this.contactRepository.findById(contact.getCid()).get();
			
			/* image */
			
			if(!file.isEmpty()) {
				/* file work */
				
				 /* delete old pic */
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				
				File file1 = new File(deleteFile,oldContactDetails.getImage());
				
				file1.delete();
				
				/*update new photo */
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
				
			}else {
				contact.setImage(oldContactDetails.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message ("Your Contact is Updated...","success"));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("conatct :" + contact.getName());
		
		return "redirect:/user/" + contact.getCid() + "/contact";
	}
	
	/* your profile handler*/
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
}
