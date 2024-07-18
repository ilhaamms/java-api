package programmerzamannow.restful.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import programmerzamannow.restful.entity.User;
import programmerzamannow.restful.model.ContactResponse;
import programmerzamannow.restful.model.CreateContactRequest;
import programmerzamannow.restful.model.UpdateContactRequest;
import programmerzamannow.restful.model.WebResponse;
import programmerzamannow.restful.service.ContactService;

@RestController
public class ContactController {

    private ContactService contactService;


    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping(
            path = "/api/contacts",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<ContactResponse> createContact(User user, @RequestBody CreateContactRequest request) {
        ContactResponse contactResponse = contactService.createContact(user, request);
        return WebResponse.<ContactResponse>builder().data(contactResponse).build();
    }

    @GetMapping(
            path = "/api/contacts/{idContact}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<ContactResponse> get(User user, @PathVariable("idContact") String idContact) {
        ContactResponse contactResponse = contactService.get(user, idContact);
        return WebResponse.<ContactResponse>builder().data(contactResponse).build();
    }

    @PutMapping(
            path = "/api/contacts/{idContact}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<ContactResponse> update(
            User user,
            @PathVariable("idContact") String idContact,
            @RequestBody UpdateContactRequest request
    ) {

        request.setId(idContact);

        ContactResponse contactResponse = contactService.update(user, request);
        return WebResponse.<ContactResponse>builder().data(contactResponse).build();
    }

    @DeleteMapping(
            path = "/api/contacts/{contactId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(User user, @PathVariable("contactId") String contactId) {
        contactService.delete(user, contactId);
        return WebResponse.<String>builder().data("OK").build();
    }

}
