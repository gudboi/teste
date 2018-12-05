package pt.ipbeja.aula5;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.ipbeja.aula5.data.db.ChatDatabase;
import pt.ipbeja.aula5.data.entity.Contact;
import pt.ipbeja.aula5.data.entity.Message;

public class ChatActivity extends AppCompatActivity {

    public static final String CONTACT_ID = "contactId";

    private long contactId;

    private MessageAdapter messageAdapter;

    private EditText messageInput;


    public static void start(Context context, long contactId) {
        Intent starter = new Intent(context, ChatActivity.class);
        starter.putExtra(CONTACT_ID, contactId);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Vamos buscar o id do contacto ao intent
        contactId = getIntent().getLongExtra(CONTACT_ID, 0);

        // se o id for < 0, significa que é inválido e terminamos aqui a Activity pois não podemos continuar sem um id válido
        if(contactId < 1) {
            finish();
            return;
        }

        messageInput = findViewById(R.id.message_text_input);
        RecyclerView messageList = findViewById(R.id.message_list);

        messageAdapter = new MessageAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(this);

        llm.setStackFromEnd(true); // podemos dizer ao LayoutManager para colocar os itens a partir do fundo da lista
        // atenção que isto não troca a ordem dos itens (ligue e desligue a funcionalidade para ver a diferença)

        messageList.setAdapter(messageAdapter);
        messageList.setLayoutManager(llm);




    }

    @Override
    protected void onStart() {
        super.onStart();
        // Vamos buscar o contacto à BD para colocar o seu nome na toolbar
        Contact contact = ChatDatabase.getInstance(this).contactDao().getContact(contactId);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) { // é "possível" que a toolbar seja null (na verdade neste caso nunca é)
            getSupportActionBar().setTitle(contact.getName());
        }

        messageInput.setHint(getString(R.string.send_message_hint, contact.getName()));

        // Vamos buscar as mensagens deste contacto
        List<Message> messages = ChatDatabase.getInstance(this).messageDao().getMessagesForContact(contactId);
        // E colocamos no adapter
        messageAdapter.setData(messages);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Ver MainActivity
        getMenuInflater().inflate(R.menu.chat, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Ver MainActivity
        switch (item.getItemId()) {

            case R.id.delete_messages:
                showDeleteMessagesDialog();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteMessagesDialog() {
        // Ver MainActivity
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.delete_msgs_dialog_title)
                .setMessage(R.string.delete_msgs_dialog_message)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        deleteAllMessages();

                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false).create();

        dialog.show();
    }

    private void deleteAllMessages() {
        int deletedCount = ChatDatabase.getInstance(ChatActivity.this)
                .messageDao()
                .deleteMessagesForContact(contactId);
        messageAdapter.deleteAll();

        // Ver Plurals em strings.xml (https://developer.android.com/guide/topics/resources/string-resource#Plurals)
        String quantityString = getResources().getQuantityString(R.plurals.deleted_messages_count_toast, deletedCount, deletedCount);
        Toast.makeText(this, quantityString, Toast.LENGTH_SHORT).show();
    }

    public void sendMessage(View view) {
        // Vamos buscar o texto que está na EditText
        String text = messageInput.getText().toString();


        // Só enviamos a mensagem se o utilizador realmente escreveu qualquer coisa
        // Também podiamos desabilitar o botão de enviar mensagens enquanto este EditText está vazio! (ver EditText#addTextChangedListener)
        if(!text.isEmpty()) {
            Message message = new Message(0, contactId, text);
            long messageId = ChatDatabase.getInstance(this).messageDao().insert(message);
            message.setId(messageId);
            messageAdapter.addMessage(message);
            messageInput.setText(""); // Limpamos o EditText para o utilizador poder escrever outra mensagem
        }
    }


    class MessageViewHolder extends RecyclerView.ViewHolder {

        Message message;
        final TextView text;

        private MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.message_text);

        }

        private void bind(Message message) {
            this.message = message;
            this.text.setText(message.getText());
        }

    }


    class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

        private List<Message> data = new ArrayList<>();


        private void addMessage(Message message) {
            // Adicionamos a mensagem à fonte de dados e notificamos o adapter que foi adicionado um item na última posição da lista
            this.data.add(message);
            notifyItemInserted(data.size()-1);
        }

        private void setData(List<Message> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item, viewGroup, false);

            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int position) {
            Message message = data.get(position);
            messageViewHolder.bind(message);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }


        private void deleteAll() {
            // Temos de guardar o tamanho da lista antes de a limpar ...
            int count = data.size();
            data.clear();
            // Para podermos notificar o adapter de quantos itens foram eliminados
            notifyItemRangeRemoved(0, count); // posição inicial do intervalo e quantos itens foram eliminados
        }
    }



}
