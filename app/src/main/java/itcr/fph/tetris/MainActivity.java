package itcr.fph.tetris;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.widget.GridLayout;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {
    int[][] MatrizTetris, PiezaActual;
    int cantHijos,cantColumnas,cantFilas, filaPiezaActual,columnaPiezaActual,NumPiezaActual;
    GridLayout Tablero;
    Boolean PiezaActiva;
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

    int [][][] RotacionPieza0=
            {{      {0,1,0},
                    {1,1,1}},

            {       {1,0},
                    {1,1},
                    {1,0}},

                {   {1,1,1},
                    {0,1,0}},

                {   {0,1},
                    {1,1},
                    {0,1}}};

    int [][][] RotacionPieza1=
            {{  {1,0,0},
                {1,1,1}},

            {   {1,1},
                {1,0},
                {1,0}},

            {   {1,1,1},
                {0,0,1}},

            {   {0,1},
                {0,1},
                {1,1}}};

    int [][][] RotacionPieza2=
            {{{1,1,1,1}},

                {   {1},
                    {1},
                    {1},
                    {1}}};

    int [][][] RotacionPieza3=
            {{  {0,0,1},
                {1,1,1}},

            {   {1,0},
                {1,0},
                {1,1}},

            {   {1,1,1},
                {1,0,0}},

            {   {1,1},
                {0,1},
                {0,1}}};

    int [][][] RotacionPieza4=
            {{  {1,1,0},
                {0,1,1}},

            {   {0,1},
                {1,1},
                {1,0}}};

    int [][][] RotacionPieza5=
            {{  {0,1,1},
                {1,1,0}},

            {   {1,0},
                {1,1},
                {0,1}}};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Preparaciones, se inicializan variables
        Tablero=(GridLayout) findViewById(R.id.Tablero);
        cantFilas=Tablero.getRowCount();
        cantColumnas=Tablero.getColumnCount();
        cantHijos=cantFilas*cantColumnas;

        MatrizTetris = new int[cantFilas][cantColumnas];

        PiezaActiva=false;

        CrearTablero();
        NuevoJuego();

    }

    public void CrearTablero(){
        //Se generan los imageViews necesarios
        for (int i=0;i<cantHijos;i++) {
            ImageView imageView=new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, 100);
            lp.setMargins(1, 1, 1, 1);
            Tablero.addView(imageView,i,lp);
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

        //Se crea un cuadro de dialogo el cual espera que el usuario este listo para comenzar el juego
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Presione comenzar cuando este listo")
                .setTitle("Nuevo Juego");

        builder.setPositiveButton("Comenzar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Comienza juego
                NuevaPieza();
            }
        });

        AlertDialog mensajeNuevoJuego = builder.create();
        mensajeNuevoJuego.show();
    }

    public void NuevaPieza(){
        PiezaActiva=true;

        //Revisa si hay lineas completas para eliminar
        EliminarLinea();

        //Elije pieza random
        Random random = new Random();
        int numPieza = random.nextInt(PosiblesPiezas.length);

        int[][] pieza = PosiblesPiezas[numPieza];
        Log.i("Prueba num pieza:", Integer.toString(numPieza));

        NumPiezaActual=numPieza;
        PiezaActual=pieza;
        columnaPiezaActual = (cantColumnas / 2) - (pieza.length-1 / 2);
        filaPiezaActual = 1;
        if(VerificacionPiezaMovimiento(PiezaActual,filaPiezaActual,columnaPiezaActual,1) && VerificacionPiezaMovimiento(PiezaActual,filaPiezaActual,columnaPiezaActual,2) && VerificacionPiezaMovimiento(PiezaActual,filaPiezaActual,columnaPiezaActual,3))  {
            MoverPieza(pieza, -1, -1, -1, -1);
            CaidaAutomaticaPieza();
        }
        else
            Derrota();
    }

    public void CaidaAutomaticaPieza(){
        //Mediante el runnable la pieza va cayendo automaticamente cada segundo
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler();
        final Runnable caidaPieza = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1500);

                if(!MoverPiezaAbajo(PiezaActual)) {//Verificacion para move abajo
                    PiezaActiva=false;
                    NuevaPieza();
                    handler.removeCallbacks(this);
                }

            }
        };

        handler.post(caidaPieza);


    }

    private void Derrota()
    {
        MoverPieza(PiezaActual, -1, -1, -1, -1);
        PiezaActiva=false;
        //Se crea un cuadro de dialogo el cual espera que el usuario este listo para comenzar el juego
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Ha perdido!")
                .setTitle("Derrota");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Comienza juego
                NuevoJuego();
            }
        });

        AlertDialog mensajeDerrota = builder.create();
        mensajeDerrota.show();
    }

    private boolean VerificacionPiezaMovimiento(int[][] pieza, int filaInicial, int columnaInicial, int tipo /*1:Abajo 2:Derecha 3: Izquierda*/) {
        boolean posicionInicialColumna = false, posicionInicialFila = false, seguirCiclo=true, resultado=true, tipoCorrecto=false;
        int contFilaPieza=0, contColumnaPieza=0;
        for (int i = 0; i < MatrizTetris.length && seguirCiclo; i++) {
            for (int j = 0; j < MatrizTetris[i].length && seguirCiclo; j++) {
                if (i == filaInicial)
                    posicionInicialFila = true;
                if(j == columnaInicial)
                    posicionInicialColumna=true;

                if (posicionInicialFila && posicionInicialColumna) {
                    if (tipo == 1 && contFilaPieza + 1 == pieza.length && contColumnaPieza < pieza[contFilaPieza].length && pieza[contFilaPieza][contColumnaPieza] == 1)
                        tipoCorrecto=true;
                    else if(tipo==2 && contFilaPieza < pieza.length && contColumnaPieza+1 == pieza[contFilaPieza].length && pieza[contFilaPieza][contColumnaPieza] == 1)
                        tipoCorrecto=true;
                    else if(tipo==3 && contFilaPieza < pieza.length && contColumnaPieza-1 == -1 && pieza[contFilaPieza][contColumnaPieza] == 1)
                        tipoCorrecto=true;

                    if(tipoCorrecto){

                            if (MatrizTetris[i][j] != 0)
                                resultado = false;
                        } else if (contFilaPieza == pieza.length) {
                            seguirCiclo = false;
                        }
                        contColumnaPieza++;

                }
                tipoCorrecto=false;
            }
            posicionInicialColumna=false;
            contColumnaPieza=0;
            if(posicionInicialFila)
                contFilaPieza++;
        }

        return resultado;
    }

    public void BorrarPieza(int[][] pieza,int filaAnterior, int columnaAnterior){
        boolean posicionInicialColumna = false, posicionInicialFila = false, seguirCiclo=true;
        int contHijos = 0, contFilaPieza=0, contColumnaPieza=0;

        //Borrar pieza anterior

            //Dibujar pieza y actualizar matriz
            posicionInicialColumna = false; posicionInicialFila = false; seguirCiclo=true;
            contHijos = 0; contFilaPieza=0; contColumnaPieza=0;
            for (int i = 0; i < MatrizTetris.length && seguirCiclo; i++) {
                for (int j = 0; j < MatrizTetris[i].length && seguirCiclo; j++) {
                    if (i == filaAnterior)
                        posicionInicialFila = true;
                    if(j == columnaAnterior)
                        posicionInicialColumna=true;

                    if (posicionInicialFila && posicionInicialColumna) {
                        if (contFilaPieza < pieza.length && contColumnaPieza < pieza[contFilaPieza].length && pieza[contFilaPieza][contColumnaPieza] == 1) {
                            MatrizTetris[i][j] = 0;
                            CambiarEstadoCelda(Tablero.getChildAt(contHijos), false);
                        }
                        else if(contFilaPieza==pieza.length){
                            seguirCiclo=false;
                        }
                        contColumnaPieza++;
                    }
                    contHijos++;
                }
                posicionInicialColumna=false;
                contColumnaPieza=0;
                if(posicionInicialFila)
                    contFilaPieza++;
            }
    }

    public void BorrarPosicionPieza(int[][] pieza,int filaAnterior, int columnaAnterior){
        boolean posicionInicialColumna = false, posicionInicialFila = false, seguirCiclo=true;
        int contHijos = 0, contFilaPieza=0, contColumnaPieza=0;

        //Borrar pieza anterior

        //Dibujar pieza y actualizar matriz
        posicionInicialColumna = false; posicionInicialFila = false; seguirCiclo=true;
        contHijos = 0; contFilaPieza=0; contColumnaPieza=0;
        for (int i = 0; i < MatrizTetris.length && seguirCiclo; i++) {
            for (int j = 0; j < MatrizTetris[i].length && seguirCiclo; j++) {
                if (i == filaAnterior)
                    posicionInicialFila = true;
                if(j == columnaAnterior)
                    posicionInicialColumna=true;

                if (posicionInicialFila && posicionInicialColumna) {
                    if (contFilaPieza < pieza.length && contColumnaPieza < pieza[contFilaPieza].length && pieza[contFilaPieza][contColumnaPieza] == 1) {
                        MatrizTetris[i][j] = 0;
                    }
                    else if(contFilaPieza==pieza.length){
                        seguirCiclo=false;
                    }
                    contColumnaPieza++;
                }
                contHijos++;
            }
            posicionInicialColumna=false;
            contColumnaPieza=0;
            if(posicionInicialFila)
                contFilaPieza++;
        }

    }

    public void DibujarPosicionPieza(int[][] pieza, int filaInicial, int columnaInicial){
        //Dibujar pieza y actualizar matriz
        boolean posicionInicialColumna = false, posicionInicialFila = false, seguirCiclo=true;
        int contHijos = 0, contFilaPieza=0, contColumnaPieza=0;
        for (int i = 0; i < MatrizTetris.length && seguirCiclo; i++) {
            for (int j = 0; j < MatrizTetris[i].length && seguirCiclo; j++) {
                if (i == filaInicial)
                    posicionInicialFila = true;
                if(j == columnaInicial)
                    posicionInicialColumna=true;

                if (posicionInicialFila && posicionInicialColumna) {
                    if (contFilaPieza < pieza.length && contColumnaPieza < pieza[contFilaPieza].length && pieza[contFilaPieza][contColumnaPieza] == 1) {
                        MatrizTetris[i][j] = 1;
                    }
                    else if(contFilaPieza==pieza.length){
                        seguirCiclo=false;
                    }
                    contColumnaPieza++;
                }
                contHijos++;
            }
            posicionInicialColumna=false;
            contColumnaPieza=0;
            if(posicionInicialFila)
                contFilaPieza++;
        }

        //Actualizar posicion pieza
        filaPiezaActual=filaInicial;
        columnaPiezaActual=columnaInicial;
    }

    public void DibujarPieza(int[][] pieza, int filaInicial, int columnaInicial){
        //Dibujar pieza y actualizar matriz
        boolean posicionInicialColumna = false, posicionInicialFila = false, seguirCiclo=true;
        int contHijos = 0, contFilaPieza=0, contColumnaPieza=0;
        for (int i = 0; i < MatrizTetris.length && seguirCiclo; i++) {
            for (int j = 0; j < MatrizTetris[i].length && seguirCiclo; j++) {
                if (i == filaInicial)
                    posicionInicialFila = true;
                if(j == columnaInicial)
                    posicionInicialColumna=true;

                if (posicionInicialFila && posicionInicialColumna) {
                    if (contFilaPieza < pieza.length && contColumnaPieza < pieza[contFilaPieza].length && pieza[contFilaPieza][contColumnaPieza] == 1) {
                        MatrizTetris[i][j] = 1;
                        CambiarEstadoCelda(Tablero.getChildAt(contHijos), true);
                    }
                    else if(contFilaPieza==pieza.length){
                        seguirCiclo=false;
                    }
                    contColumnaPieza++;
                }
                contHijos++;
            }
            posicionInicialColumna=false;
            contColumnaPieza=0;
            if(posicionInicialFila)
                contFilaPieza++;
        }

        //Actualizar posicion pieza
        filaPiezaActual=filaInicial;
        columnaPiezaActual=columnaInicial;
    }

    //Muestra la pieza y cambia matriz de control
    public void MoverPieza(int[][] pieza, int filaInicial, int columnaInicial, int filaAnterior, int columnaAnterior) {
        //En caso que sea una pieza nueva
        boolean BorrarAnt=true;
        if (filaAnterior == -1 && columnaAnterior == -1) {
            columnaInicial = (cantColumnas / 2) - (pieza.length-1 / 2);
            filaInicial = 1;
            BorrarAnt=false;
        }

        //Borrar pieza anterior
        if(BorrarAnt){
           BorrarPieza(pieza,filaAnterior, columnaAnterior);
        }

        //Dibujar y actualizar matriz nueva posicion
        DibujarPieza(pieza, filaInicial, columnaInicial);

    }

    public boolean ValidarRotacionPieza(int[][] pieza, int filaActual, int columnaActual,int numPieza){
        boolean resultadoValidacion=true;
        if(!(numPieza==6)){
            int[][] nuevaRotacionPieza=new int[][]{};
            int[][][] rotacionesPiezaActual= new int[][][]{};

            //Encontrar rotaciones de la pieza actual
            if(numPieza==0)
                rotacionesPiezaActual=RotacionPieza0;
            else if(numPieza==1)
                rotacionesPiezaActual=RotacionPieza1;
            else if(numPieza==2)
                rotacionesPiezaActual=RotacionPieza2;
            else if(numPieza==3)
                rotacionesPiezaActual=RotacionPieza3;
            else if(numPieza==4)
                rotacionesPiezaActual=RotacionPieza4;
            else if(numPieza==5)
                rotacionesPiezaActual=RotacionPieza5;

            for(int i=0; i<rotacionesPiezaActual.length;i++){
                if(rotacionesPiezaActual[i].length==pieza.length && rotacionesPiezaActual[i][0].length==pieza[0].length && rotacionesPiezaActual[i][0][0]==pieza[0][0]){//No logre encontrar como comparar 2 matrices por lo que se volvio complicado este if
                    if(rotacionesPiezaActual[i][0].length>1 && pieza[0].length>1){
                        if(rotacionesPiezaActual[i][0][1]==pieza[0][1]){
                            if(i<rotacionesPiezaActual.length-1)
                                nuevaRotacionPieza=rotacionesPiezaActual[i+1];
                            else
                                nuevaRotacionPieza=rotacionesPiezaActual[0];
                        }
                    }
                    else if(rotacionesPiezaActual[i].length>1 && pieza.length>1){
                        if(rotacionesPiezaActual[i][1]==pieza[1]){
                            if(i<rotacionesPiezaActual.length-1)
                                nuevaRotacionPieza=rotacionesPiezaActual[i+1];
                            else
                                nuevaRotacionPieza=rotacionesPiezaActual[0];
                        }
                        else{
                            if(i<rotacionesPiezaActual.length-1)
                                nuevaRotacionPieza=rotacionesPiezaActual[i+1];
                            else
                                nuevaRotacionPieza=rotacionesPiezaActual[0];
                        }
                    }

                }
            }

            BorrarPosicionPieza(pieza,filaActual,columnaActual);

            boolean posicionInicialColumna = false, posicionInicialFila = false, seguirCiclo=true;
            int contHijos = 0, contFilaPieza=0, contColumnaPieza=0;
            for (int i = 0; i < MatrizTetris.length && seguirCiclo; i++) {
                for (int j = 0; j < MatrizTetris[i].length && seguirCiclo; j++) {
                    if (i == filaActual)
                        posicionInicialFila = true;
                    if(j == columnaActual)
                        posicionInicialColumna=true;

                    if (posicionInicialFila && posicionInicialColumna) {
                        if (contFilaPieza < nuevaRotacionPieza.length && contColumnaPieza < nuevaRotacionPieza[contFilaPieza].length && nuevaRotacionPieza[contFilaPieza][contColumnaPieza] == 1) {
                            if(MatrizTetris[i][j] != 0){
                                resultadoValidacion=false;
                                DibujarPosicionPieza(pieza,filaActual,columnaActual);
                            }
                        }
                        else if(contFilaPieza==nuevaRotacionPieza.length){
                            seguirCiclo=false;
                        }
                        contColumnaPieza++;
                    }
                    contHijos++;
                }
                posicionInicialColumna=false;
                contColumnaPieza=0;
                if(posicionInicialFila)
                    contFilaPieza++;
            }
        }

        return resultadoValidacion;
    }

    public void RealizarRotacionPieza(int[][] pieza, int filaActual, int columnaActual,int numPieza){
        if(!(numPieza==6)){
            //Borra la pieza anterior
            BorrarPieza(pieza,filaActual,columnaActual);

            int[][] nuevaRotacionPieza=new int[][]{};
            int[][][] rotacionesPiezaActual= new int[][][]{};

            //Encontrar rotaciones de la pieza actual
            if(numPieza==0)
                rotacionesPiezaActual=RotacionPieza0;
            else if(numPieza==1)
                rotacionesPiezaActual=RotacionPieza1;
            else if(numPieza==2)
                rotacionesPiezaActual=RotacionPieza2;
            else if(numPieza==3)
                rotacionesPiezaActual=RotacionPieza3;
            else if(numPieza==4)
                rotacionesPiezaActual=RotacionPieza4;
            else if(numPieza==5)
                rotacionesPiezaActual=RotacionPieza5;

            for(int i=0; i<rotacionesPiezaActual.length;i++){
                if(rotacionesPiezaActual[i].length==pieza.length && rotacionesPiezaActual[i][0].length==pieza[0].length && rotacionesPiezaActual[i][0][0]==pieza[0][0]){//No logre encontrar como comparar 2 matrices por lo que se volvio complicado este if
                    if(rotacionesPiezaActual[i][0].length>1 && pieza[0].length>1){
                        if(rotacionesPiezaActual[i][0][1]==pieza[0][1]){
                            if(i<rotacionesPiezaActual.length-1)
                                nuevaRotacionPieza=rotacionesPiezaActual[i+1];
                            else
                                nuevaRotacionPieza=rotacionesPiezaActual[0];
                        }
                    }
                    else if(rotacionesPiezaActual[i].length>1 && pieza.length>1){
                        if(rotacionesPiezaActual[i][1]==pieza[1]){
                            if(i<rotacionesPiezaActual.length-1)
                                nuevaRotacionPieza=rotacionesPiezaActual[i+1];
                            else
                                nuevaRotacionPieza=rotacionesPiezaActual[0];
                        }
                        else{
                            if(i<rotacionesPiezaActual.length-1)
                                nuevaRotacionPieza=rotacionesPiezaActual[i+1];
                            else
                                nuevaRotacionPieza=rotacionesPiezaActual[0];
                        }
                    }

                }
            }

            PiezaActual=nuevaRotacionPieza;
            DibujarPieza(nuevaRotacionPieza,filaActual,columnaActual);
        }
    }

    public void EliminarLinea(){
        //Verifica y elimina las lineas completas en la matriz de control
        int contColumnas;
        for(int i=MatrizTetris.length-1;i>0;i--){
            contColumnas=0;
            for(int j=MatrizTetris[i].length-1;j>0;j--){
                if(MatrizTetris[i][j] == 1)
                    contColumnas++;
            }
            if(contColumnas==cantColumnas-2){
                for(int p=i;p>1;p--) {
                    for (int k = 1; k < MatrizTetris[p].length - 1; k++) {
                        MatrizTetris[p][k] = MatrizTetris[p - 1][k];
                        if (!(p == 1))
                            MatrizTetris[p - 1][k] = 0;
                    }
                }
                i++;
            }
        }

        //Actualiza el tablero visualmente
        int contHijos=0;
        for(int i=0;i<MatrizTetris.length;i++){
            for(int j=0;j<MatrizTetris[i].length;j++){
                if(MatrizTetris[i][j] == 1)
                    CambiarEstadoCelda(Tablero.getChildAt(contHijos), true);
                else if(MatrizTetris[i][j] == 0)
                    CambiarEstadoCelda(Tablero.getChildAt(contHijos), false);
                else
                    ((ImageView)Tablero.getChildAt(contHijos)).setImageResource(R.drawable.cuadronegro);
                contHijos++;
            }
        }

    }

    public Boolean MoverPiezaAbajo(int[][] pieza){
        Boolean resultadoVerificacion= VerificacionPiezaMovimiento(pieza, filaPiezaActual + 1, columnaPiezaActual,1);
        if(PiezaActiva && resultadoVerificacion) {
            MoverPieza(pieza, filaPiezaActual + 1, columnaPiezaActual,filaPiezaActual,columnaPiezaActual);
        }

        return resultadoVerificacion;
    }

    public void MoverPiezaDerecha(int[][] pieza){
        if(PiezaActiva && VerificacionPiezaMovimiento(pieza, filaPiezaActual, columnaPiezaActual+1,2)) {
            MoverPieza(pieza, filaPiezaActual, columnaPiezaActual+1,filaPiezaActual,columnaPiezaActual);
        }
    }

    public void MoverPiezaIzquierda(int[][] pieza){
        if(PiezaActiva && VerificacionPiezaMovimiento(pieza, filaPiezaActual, columnaPiezaActual-1,3)) {
            MoverPieza(pieza, filaPiezaActual, columnaPiezaActual-1,filaPiezaActual,columnaPiezaActual);
    }
    }

    public void BTNClickAbajo(View view){
        MoverPiezaAbajo(PiezaActual);
    }

    public void BTNClickDerecha(View view){
        MoverPiezaDerecha(PiezaActual);
    }
    public void BTNClickIzquierda(View view){
        MoverPiezaIzquierda(PiezaActual);
    }
    public void BTNClickVuelta(View view) {
        if(ValidarRotacionPieza(PiezaActual,filaPiezaActual,columnaPiezaActual,NumPiezaActual))
            RealizarRotacionPieza(PiezaActual,filaPiezaActual,columnaPiezaActual,NumPiezaActual);
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
