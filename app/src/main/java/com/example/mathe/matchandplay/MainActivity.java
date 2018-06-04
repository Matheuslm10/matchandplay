package com.example.mathe.matchandplay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mathe.matchandplay.Adapter.UsuarioAdapter;
import com.example.mathe.matchandplay.BD.ConfiguracaoFireBase;
import com.example.mathe.matchandplay.ClassesObjetos.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ListView usuarioListView;
    private ArrayAdapter<Usuario> adapter;
    ArrayList<Usuario> arrayListUsuario;
    private DatabaseReference firebase;
    private ValueEventListener valueEventListenerUsuarios;
    private FirebaseAuth usuarioFirebase;

    //dados no menu lateral
    private TextView nomeUsuario;
    private TextView emailUsuario;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Match's");

        firebase = ConfiguracaoFireBase.getFireBase().child("usuario");
        usuarioFirebase = ConfiguracaoFireBase.getFirebaseAutenticacao();

        //relacionando a ListView com o Adapter de Usuarios
        arrayListUsuario = new ArrayList<>();
        usuarioListView = findViewById(R.id.usuariosList);
        adapter = new UsuarioAdapter(this, arrayListUsuario);
        usuarioListView.setAdapter(adapter);


        //colocando os dados do usuario logado no menu lateral
        View header = ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0);
        nomeUsuario = (TextView) header.findViewById(R.id.textViewNomeUsuario);
        emailUsuario = (TextView) header.findViewById(R.id.textViewEmailUsuario);
        Query query = firebase.orderByChild("email").equalTo(usuarioFirebase.getCurrentUser().getEmail());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        Usuario users = issue.getValue(Usuario.class);
                        nomeUsuario.setText(users.getNomeusuario());
                        emailUsuario.setText(users.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //preenchendo a lista dos match's
        valueEventListenerUsuarios = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayListUsuario.clear();

                for (DataSnapshot dados : dataSnapshot.getChildren()) {
                    Usuario novosUsuarios = dados.getValue(Usuario.class);

                    arrayListUsuario.add(novosUsuarios);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        usuarioListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Usuario usuarioSelecionado = adapter.getItem(position);
                Intent it = new Intent(MainActivity.this, MostraUsuario.class);
                it.putExtra("email_user_selected", usuarioSelecionado.getEmail());
                startActivity(it);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent it = new Intent(this, MeusJogos.class);
            startActivity(it);

        } else if (id == R.id.nav_gallery) {
            Intent it = new Intent(this, JogosDesejados.class);
            startActivity(it);

        } else if (id == R.id.nav_slideshow) {
            Intent it = new Intent(this, Faq.class);
            startActivity(it);

        } else if (id == R.id.nav_manage) {
            deslogarUsuario();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void deslogarUsuario() {
        usuarioFirebase.signOut();
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerUsuarios);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebase.addValueEventListener(valueEventListenerUsuarios);
    }


}
