package itcr.fph.tetris;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.support.v7.widget.GridLayout;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    int[][] MatrizTetris;
    int cantHijos,cantColumnas,cantFilas;
    GridLayout Tablero;
    int [][][] PosiblesPiezas=
            {{  {0,1,0},
                {1,1,1}},

            {   {1,0,0},
                {1,1,1}},

            {   {1,1,1,1}},

            {   {0,0,1},
                {1,1,1}},

            {   {1,1,0},
                {0,1,1}},

            {   {0,1,1},
                {1,1,0}},

            {   {1,1},
                {1,1}}
            };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Preparaciones
        Tablero=(GridLayout) findViewById(R.id.Tablero);
        cantFilas=Tablero.getRowCount();
        cantColumnas=Tablero.getColumnCount();
        cantHijos=cantFilas*cantColumnas;

        MatrizTetris = new int[cantFilas][cantColumnas];

        CrearTablero();
        NuevoJuego();

    }

    public void CrearTablero(){
        //Se generan los imageViews necesarios
        for (int i=0;i<cantHijos;i++) {
            ImageView imageView=new ImageView(this);
            Tablero.addView(imageView,i,new ViewGroup.LayoutParams(100, 100));
            imageView.setTag(i);
        }
    }

    public void NuevoJuego(){

        //Llenar matriz para controlar tablero a la vez que se crea el marco y se resetea las imgs del tablero
        int contHijos=0;
        for(int i=0;i<MatrizTetris.length;i++){
            for(int j=0;j<MatrizTetris[i].length;j++){
                if(i==0 || i==MatrizTetris.length-1 || j==0 || j==MatrizTetris[i].length-1) {
                    MatrizTetris[i][j] = -1;
                    ((ImageView)Tablero.getChildAt(contHijos)).setImageResource(R.drawable.cuadronegro);
                }
                else {
                    MatrizTetris[i][j] = 0;
                    CambiarEstadoCelda(Tablero.getChildAt(contHijos), false);
                }
                contHijos++;
            }
        }

        //Resetear tablero en caso de que se estuviera en un juego previamente
        for (int i=0;i<=cantHijos;i++) {


        }

        //Se crea un cuadro de dialogo el cual espera que el usuario este listo para comenzar el juego
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Presione comenzar cuando este listo")
                .setTitle("Nuevo Juego");

        builder.setPositiveButton("Comenzar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ComenzarJuego();
            }
        });

        AlertDialog mensajeNuevoJuego = builder.create();
        mensajeNuevoJuego.show();
    }

    public void ComenzarJuego(){

    }

    public void CambiarEstadoCelda(View celda,boolean estado){
        ImageView imageView=(ImageView) celda;
        if(estado){
            imageView.setImageResource(R.drawable.cuadroazul);
        }
        else{
            imageView.setImageResource(0);
        }
    }


//Toast.makeText(getApplicationContext(),"Prueba",Toast.LENGTH_LONG).show();


}
