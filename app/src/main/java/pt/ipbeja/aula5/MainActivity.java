package pt.ipbeja.aula5;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pt.ipbeja.aula5.data.db.ChatDatabase;
import pt.ipbeja.aula5.data.entity.Contact;
import pt.ipbeja.aula5.prefs.PreferencesHelper;

import static pt.ipbeja.aula5.prefs.PreferencesHelper.*;

public class MainActivity extends AppCompatActivity {

    private static final String MAIN_PREFS = "main_prefs";
    private static final String FIRST_TIME_PREF = "first_time";
    private static final String SORTING_PREF = "sort_pref";


    private RecyclerView contactList;
    private ContactAdapter contactAdapter;
    private LinearLayoutManager linearLayoutManager;

    private View addContactHint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Podemos colocar um icon na toolbar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true); // ligar essa funcionalidade
            getSupportActionBar().setIcon(R.mipmap.ic_launcher_round); // colocar o icon
        }


        addContactHint = findViewById(R.id.add_contact_hint_wrapper);
        FloatingActionButton createContactFab = findViewById(R.id.create_contact_fab);
        contactList = findViewById(R.id.contact_list);


        createContactFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Um click longo no FAB, mostra o que o botão faz ao utilizador
                Toast.makeText(MainActivity.this, R.string.create_contact_btn_hint, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        contactAdapter = new ContactAdapter();
        linearLayoutManager = new LinearLayoutManager(this);

        contactList.setAdapter(contactAdapter);
        contactList.setLayoutManager(linearLayoutManager);


    }

    @Override
    protected void onStart() {
        super.onStart();
        List<Contact> contacts = ChatDatabase.getInstance(this).contactDao().getAllContacts();

        contactAdapter.setData(contacts, true);
        setAddContactHintVisible(contacts.size() == 0); // Se o número de contactos é 0, mostramos a View com a dica para adicionar contactos

    }


    /**
     * Define a visibilidade da View que indica que não existem contactos (ver activity_main.xml)
     * @param visible Visível
     */
    private void setAddContactHintVisible(boolean visible) {
        // Existem 3 modos de visibilidade possíveis: VISIBLE, INVISIBLE e GONE
        // São constantes (int) definidas na class View
        addContactHint.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Fazer inflate do xml do menu

        getMenuInflater().inflate(R.menu.main, menu);
        boolean contactListSort = getContactListSort(this);
        MenuItem item = menu.findItem(R.id.sort_a_z);
        item.setChecked(contactListSort);

        //if(menu instanceof MenuBuilder) ((MenuBuilder) menu).setOptionalIconsVisible(true);
        // Temos de devolver true para o menu aparecer
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //boolean Sorting = getSharedPreferences(MAIN_PREFS,MODE_PRIVATE).getBoolean(SORTING_PREF, true);
        boolean contactListSort = getContactListSort(this);
        MenuItem item = menu.findItem(R.id.sort_a_z);
        item.setChecked(contactListSort);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Método de callback de interação com os itens do menu


        switch (item.getItemId()) { // Vamos buscar o id do item...
            case R.id.delete_contacts:
                showDeleteAllContactsDialog();
                return true; // Devolvemos true se tratámos desta interação
            case R.id.contacts_map:
                showContactsMap();
                return true;
            case R.id.sort_a_z:
                contactAdapter.sort(true);
                item.setChecked(true);
                return true;
            case R.id.sort_z_a:
                contactAdapter.sort(false);
                boolean contactListSort;
                setContactListSort(this,false);
                item.setChecked(true);
                return true;
        }

        return super.onOptionsItemSelected(item); // caso contrário, deixamos a Activity procurar outro possível "handler"
    }

    private void showContactsMap() {
        ContactsMapActivity.start(this);
    }


    private void showDeleteAllContactsDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.delete_contacts_dialog_title)
                .setMessage(R.string.delete_contacts_dialog_message)
                .setPositiveButton(R.string.delete_contact_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAllContacts();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null) // Se não precisamos de tratar o evento, podemos passar null
                .setCancelable(false) // cancelable a false evita que o utilizador possa dispensar o dialog pressionando fora da caixa deste
                .create();

        dialog.show(); // não esquecer invocar o método para exibir o diálogo
    }

    private void deleteAllContacts() {
        int deletedCount = ChatDatabase.getInstance(this).contactDao().deleteAll();

        // Ver Plurals em strings.xml (https://developer.android.com/guide/topics/resources/string-resource#Plurals)
        String quantityString = getResources().getQuantityString(R.plurals.deleted_contacts_count_toast, deletedCount, deletedCount);

        Toast.makeText(this, quantityString, Toast.LENGTH_SHORT).show();
        contactAdapter.removeAll();
        setAddContactHintVisible(true);
    }


    private void showDeleteContactDialog(final Contact contact) {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.delete_contact_dialog_title)
                .setMessage(getString(R.string.delete_contact_dialog_message, contact.getName()))
                .setPositiveButton(R.string.delete_contact_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteContact(contact);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.show();
    }

    private void deleteContact(Contact contact) {
        // Prints demonstram que ao eliminar o contacto da BD também as 'suas' mensagens são eliminadas (ver ForeignKey em Message)
        int msgCount = ChatDatabase.getInstance(this).messageDao().messageCountForContact(contact.getId());
        System.out.println("Before removing contact: " + msgCount + " messages.");
        ChatDatabase.getInstance(this).contactDao().delete(contact);
        msgCount = ChatDatabase.getInstance(this).messageDao().messageCountForContact(contact.getId());
        System.out.println("After removing contact: " + msgCount + " messages.");

        contactAdapter.remove(contact);
        setAddContactHintVisible(contactAdapter.getItemCount() == 0);
    }


    private void startChatActivity(Contact contact) {
        // Ao iniciar a ChatActivity, passamos o id do contacto que essa activity vai representar
        ChatActivity.start(this, contact.getId());
    }

    public void createContact(View view) {
        CreateContactActivity.start(this);
    }



    class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        Contact contact;

        final TextView name;
        final TextView initials;
        final ImageView photo; // Se quiserem utilizar ImageViews circulares, podem utilizar a biblioteca CircleImageView (https://github.com/hdodenhof/CircleImageView)
        final View initialsChip;


        private ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this); // também existe um Listener para clicks longos! ver #onLongClick abaixo
            name = itemView.findViewById(R.id.contact_name);
            initials = itemView.findViewById(R.id.contact_initials);
            photo = itemView.findViewById(R.id.contact_photo);
            initialsChip = itemView.findViewById(R.id.contact_chip);
        }

        private void bind(Contact contact) {
            this.contact = contact;
            name.setText(contact.getName());
            if(contact.getPhoto() != null && contact.getPhoto().length != 0) {
                photo.setImageBitmap(bitmapFromBytes(contact.getPhoto()));
                initialsChip.setVisibility(View.INVISIBLE);
                photo.setVisibility(View.VISIBLE);
            }
            else {
                initials.setText(contact.getInitials());
                initialsChip.setVisibility(View.VISIBLE);
                photo.setVisibility(View.INVISIBLE);
            }

        }

        private Bitmap bitmapFromBytes(byte[] photoBytes) {
            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(photoBytes);
            Bitmap bitmap = BitmapFactory.decodeStream(arrayInputStream);
            return bitmap;
        }

        @Override
        public void onClick(View view) {
            startChatActivity(contact); // um click curto, lança a Activity com as mensagens do contacto
        }

        @Override
        public boolean onLongClick(View view) {
            showDeleteContactDialog(contact); // um click longo, invocamos o método para apagar o contacto
            return true; // devolvemos true se tratámos o evento
        }
    }




    class ContactAdapter extends RecyclerView.Adapter<ContactViewHolder> {

        private List<Contact> data = new ArrayList<>();

        private void setData(List<Contact> data, boolean sort) {
            this.data = data;
            sort(sort);
        }

        private void sort(final boolean asc) {

            Collections.sort(data, new Comparator<Contact>() {
                @Override
                public int compare(Contact o1, Contact o2) {
                    int sort = o1.getName().compareTo(o2.getName());
                    if(asc) return sort;
                    else return -sort;
                }
            });
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_list_item, viewGroup, false);
            return new ContactViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder contactViewHolder, int i) {
            Contact contact = data.get(i);
            contactViewHolder.bind(contact);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private void remove(Contact contact) {
            int index = data.indexOf(contact);
            if(index != -1) {
                data.remove(index);
                notifyItemRemoved(index);
            }
        }

        private void removeAll() {
            int count = data.size();
            if(count > 0) {
                data.clear();
                notifyItemRangeRemoved(0, count);
            }
        }
    }

}
