package proy_arqui;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Procesador extends Thread { 
    
    private static Multiprocesador myMp;
    private Estructuras estr;
    private int myNumP; 
    
    private int pcA;
    private int limit;
    
    private boolean puedoSeguir = true;
    private boolean final_programa = false;
    private int ciclo = 0;
    
    private int stop = 0;
    private int cont = 0;
    
    private int destruir = 0;
    
    //COLUMNAS EN CACHE
    private final int ID = 0;
    private final int EST = 1;
    //ESTADOS DE BLOQUES
    private final int C = 0;
    private final int M = 1;
    private final int I = 2;
    private final int U = 3;
    
    private final int E = 1;
    
    private int PC;                             // Contador de programa
    private int IR;                             // Registro de instruccion
    private int regs[] = new int[32];           // 32 registros
    // Para la cache de datos agregamos dos filas extra que hacen referencia al número de bloque y al estado del bloque ('C','M','I')
    //private int dcache[][] = new int[4][4];     // Cache de datos (4 bloques, cada bloque con 4 palabras, cada palabra 4 bytes)
    //private int estCache[][] = new int[4][2];   // 8bloques*4 = 32 palabras ---> 32palabras*4 = 128 direcciones de palabras
    
    //private int dmem[] = new int[32];           // Memoria de datos compartida (8 bloques, cada uno con 4 palabras)    
    
    //constructor
    public Procesador(int numP, Multiprocesador mp, Estructuras es){
        myMp = mp;
        estr = es; 
        myNumP = numP;
        for(int x=0; x < 4; ++x){
            setEstBloqueCache(x, I);
            setIdBloqueCache(x, -1);
            //estCache[x][EST] = I;
            //estCache[x][ID] = -1;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //SECCION DE SETS Y GETS, NO IMPORTA DE DONDE VENGA LA MEMORIA Y CACHE, LOS CAMBIOS
    //SE HACEN SOLO ACA Y NO EN EL RESTO DEL CODIGO
    
    public void setPalabraCache(int indiceBloque, int indicePalabra, int valor){
        estr.setPalabraCache(myNumP,indiceBloque,indicePalabra,valor);
        //dcache[indiceBloque][indicePalabra] = valor;
    }
    
    public int getPalabraCache(int indiceBloque, int indicePalabra){
        return estr.getPalabraCache(myNumP,indiceBloque,indicePalabra);
        //return dcache[indiceBloque][indicePalabra];
    }
    
    public void setEstBloqueCache(int indiceBloque, int estado){
        estr.setEstBloqueCache(myNumP,indiceBloque,estado);
        //estCache[indiceBloque][EST] = estado;
    }
    
    public int getEstBloqueCache(int indiceBloque){
        return estr.getEstBloqueCache(myNumP,indiceBloque);
        //return estCache[indiceBloque][EST];
    }
    
    public void setIdBloqueCache(int indiceBloque, int id){
        estr.setIdBloqueCache(myNumP,indiceBloque, id);
        //estCache[indiceBloque][ID] = id;
    }
    
    public int getIdBloqueCache(int indiceBloque){
        return estr.getIdBloqueCache(myNumP,indiceBloque);
        //return estCache[indiceBloque][ID];
    }
    
    public void setPalabraMem(int indiceMem, int valor){
        estr.setPalabraMem(myNumP, indiceMem, valor);
        //dmem[indiceMem] = valor;
    }
    
    public int getPalabraMem(int indiceMem){
        return estr.getPalabraMem(myNumP, indiceMem);
    }
    
    
    // Cambia el estado de un bloque en el directorio
    // RECIBE: número del directorio, id de un bloque en memoria, nuevoEstado
    public void setEstDir(int numDir, int idBloque, int nuevoEstado){
        int indiceDir = -1;
        if(idBloque >= 0 && idBloque <=31){
            indiceDir = idBloque/4;
        }
        if(idBloque >= 32 && idBloque <=63){
            indiceDir = (idBloque-32)/4;
        }
        if(idBloque >= 64 && idBloque <=95){
            indiceDir = (idBloque-64)/4;
        }
        estr.setEntradaDir(numDir, indiceDir, E, nuevoEstado);
    }
    
    public int getEstDir(int idBloque){
        return estr.getEstadoBloqueDir(idBloque);
    }
    
    //FIN DE LA SECCION DE SETS Y GETS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    // Copia el bloque entero en el lugar que le corresponde en cache
    public void cargarACache(int direccionMemoria, int direccionCache){
        int j = direccionMemoria;
        for(int i = 0; i < 4; i++){
            setPalabraCache(direccionCache, i, getPalabraMem(j));
            //setPalabraCache(direccionCache, i, dmem[j]);
            //dcache[direccionCache][i] = dmem[j];
            j++;
        }
        
        for(int i = 0; i<16; i++){
            puedoSeguir = false;
            try {
                myMp.barrera.await();
                ciclo++; cont++;
                //System.out.println("Ciclo #"+ciclo+". No puede cambiar de instrucción.");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        
        puedoSeguir = true;
        //System.out.println("Ya puede cambiar de instrucción.");
        
    }
    
    // Guarda el bloque entero desde cache hasta el lugar en memoria que le corresponde
    public void guardarEnMemoria(int direccionMemoria, int direccionCache){
        int j = direccionMemoria;
        for(int i = 0; i < 4; i++){
            setPalabraMem(j, getPalabraCache(direccionCache, i)); 
            //dmem[j] = getPalabraCache(direccionCache, i);
            //dmem[j] = dcache[direccionCache][i];
            j++;
        }
        
        for(int i = 0; i<16; i++){
            puedoSeguir = false;
            try {
                myMp.barrera.await();
                ciclo++; cont++;
                //System.out.println("Ciclo #"+ciclo+". No puede cambiar de instrucción.");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        
        puedoSeguir = true;
        //System.out.println("Ya puede cambiar de instrucción.");
    }
    
    public void pedirParaLeer(int idBloque, int direccionMemoria, int direccionCache){
        int papa = estr.directorioPapa(idBloque); //1, 2, 3
        int estado = estr.getEstadoBloqueDir(idBloque); // 'C', 'M', 'U'
        //int duenoBloque = estr.consultarDuenoBloqueDir(myNumP,idBloque);
        switch(estado){
            case C:
                //estr.cargarACache(int numCache, int direccionMemoria, int direccionCache);
                cargarACache(direccionMemoria, direccionCache);
                estr.anadirProcesador(idBloque, myNumP);
                // cargo el bloque en mi cache desde memoria
                // agregarme a la lista de compartidos
            break;
            case M:
                int idDueno = estr.consultarDuenoBloqueDir(idBloque);
                estr.guardarEnMemoria(idDueno, direccionMemoria, direccionCache);    // hasta aqui llegamos 
                estr.setEntradaDir(idDueno, (direccionMemoria/4), 1, C);
                // subir lo de la cache dueña en memoria
                // poner el directorio como compartido
                // bajar lo de memoria a cache
                // agregar en la lista de compartidos
            break;
            case U:
                
            break; 
        }
    }
    
    // Leer una palabra
    public void LW(int Y, int X, int n){
        int numByte = regs[Y]+n;                                // Numero del byte que quiero leer de memoria 
        int numBloqMem = Math.floorDiv(numByte,16);             // Indice del bloque en memoria (0-24)
        int numPalabra = (numByte%16)/4;
        int dirBloqCache = numBloqMem%4;                        // Indice donde debe estar el bloque en cache
        estr.waitC(myNumP);                                     // bloque mi cache ya que la voy a usar
        int idBloqEnCache = getIdBloqueCache(dirBloqCache);        // ID del bloque que ocupa actualmente esa direccion en cache
        //int idBloqEnCache = estCache[dirBloqCache][ID];         // ID del bloque que ocupa actualmente esa direccion en cache
        
        int estadoBloqEnCache = getEstBloqueCache(dirBloqCache);  // Estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        //int estadoBloqEnCache = estCache[dirBloqCache][EST];    // Estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        int dirNumBloqMem = numBloqMem*4;                       // Conversion para mapear la direccion inicial del bloque en memoria

        // CASO 1: el bloque que requerimos no esta en cache, en su lugar hay otro bloque
        if(idBloqEnCache != dirNumBloqMem){
            // El id del bloque que esta ocupando cache es -1 (no hay bloque) o es otro bloque
            switch(estadoBloqEnCache){
                case C:
                    // Nos traemos el bloque de memoria a cache
                    cargarACache(dirNumBloqMem, dirBloqCache);
                    setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                    setEstBloqueCache(dirBloqCache, C);
                    //estCache[dirBloqCache][ID] = dirNumBloqMem; // Bloque que ocupa ahora esa direccion de cache
                    //estCache[dirBloqCache][EST] = C;            // Estado del bloque que ocupa ahora esa direccion de cache
                    regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                    estr.signalC(myNumP);
                break;
                case M:
                    if(estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0){ // esto es para saber si el directorio que voy a utilizar esta ocupado o no
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    }else{
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        guardarEnMemoria(getIdBloqueCache(dirBloqCache), dirBloqCache); 
                        //guardarEnMemoria(estCache[dirBloqCache][ID], dirBloqCache);
                        
                        //estCache[dirBloqCache][EST] = C;
                        //guardarEnMemoria(dirNumBloqMem, dirBloqCache);   // Guarda el bloque está ahora en cache a su posicion en memoria 
                        cargarACache(dirNumBloqMem, dirBloqCache);
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, C);

                        //estCache[dirBloqCache][ID] = dirNumBloqMem;         // Bloque que ocupa ahora esa direccion de cache
                        //estCache[dirBloqCache][EST] = C;                    // Estado del bloque que ocupa ahora esa direccion de cache
                        
                        setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C); //pero tengo q poner para quienes esta C
                        //estr.quitarCompartidos(numBloqMem, myNumP);
                        estr.anadirProcesador(numBloqMem, myNumP);
                        
                        regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                        
                        estr.signalC(myNumP);
                        estr.signalD(estr.directorioPapa(numBloqMem));
                    }
                break;
                case I:        // QUEDAMOS AQUI
                    if(estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0){ // esto es para saber si el directorio que voy a utilizar esta ocupado o no
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    }else{
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        
                        if(estr.getEstadoBloqueDir(numBloqMem) == C){
                            //guardarEnMemoria(getIdBloqueCache(dirBloqCache), dirBloqCache); 
                            //guardarEnMemoria(estCache[dirBloqCache][ID], dirBloqCache);

                            //estCache[dirBloqCache][EST] = C;
                            //guardarEnMemoria(dirNumBloqMem, dirBloqCache);   // Guarda el bloque está ahora en cache a su posicion en memoria 
                            cargarACache(dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, C);

                            //estCache[dirBloqCache][ID] = dirNumBloqMem;         // Bloque que ocupa ahora esa direccion de cache
                            //estCache[dirBloqCache][EST] = C;                    // Estado del bloque que ocupa ahora esa direccion de cache

                            estr.anadirProcesador(numBloqMem, myNumP);
                            //estr.quitarCompartidos(numBloqMem, myNumP);

                            regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro

                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(numBloqMem));
                        }else if(estr.getEstadoBloqueDir(numBloqMem) == M){
                            int cacheDuena = estr.consultarDuenoBloqueDir(numBloqMem);
                            if(estr.disponibleC(cacheDuena) == 0){
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(numBloqMem));
                                LW(Y, X, n);
                            }else{
                                estr.waitC(cacheDuena);  //si la logro agarrar
                                
                                estr.guardarEnMemoria(cacheDuena, numBloqMem, dirBloqCache);
                                estr.setEstBloqueCache(cacheDuena, numBloqMem, C);
                                estr.signalC(cacheDuena);

                                setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C); //pero tengo q poner para quienes esta C


                                cargarACache(dirNumBloqMem, dirBloqCache);
                                setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                                setEstBloqueCache(dirBloqCache, C);

                                //estCache[dirBloqCache][ID] = dirNumBloqMem;         // Bloque que ocupa ahora esa direccion de cache
                                //estCache[dirBloqCache][EST] = C;                    // Estado del bloque que ocupa ahora esa direccion de cache

                                estr.anadirProcesador(numBloqMem, myNumP);
                                //estr.quitarCompartidos(numBloqMem, myNumP);

                                regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                                //agarro la cache donde esta el bloque
                                //subir ese bloque de cache 'otra' a memoria
                                //pongo en cache 'otra' a ese bloque en C
                                //libero cache 'otra'
                                //limpio el  directorio
                                //pongo a procesador 'otro' como compartido
                                //me pongo yo como compartido
                                //guardo en registro
                                //libero los 2

                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(numBloqMem));
                            }
                        }else{ // el caso en que este uncached y aqui terminamos antes de que iva lo haga mas complicado el trabajo
                            
                        }
                    }

                    // Buscar dueño 
                    //pedirParaLeer(dirNumBloqMem);

                    // Busco el estado del bloque en la cache del dueño 
                    // Pido bloque para leer
                    // caso 'C': 
                        // cargo el bloque en mi cache desde memoria
                        // agregarme a la lista de compartidos
                    // caso 'M': 
                        // subir lo de la cache dueña en memoria
                        // poner el directorio como compartido
                        // bajar lo de memoria a cache
                        // agregar en la lista de compartidos
                break;
            }
        }else{ // HIT :D 
            switch(estadoBloqEnCache){
                case C:
                    // Si está compartido solo queda cargar la palabra al registro
                break;
                case M:
                    guardarEnMemoria(dirNumBloqMem, dirBloqCache);
                    setEstBloqueCache(dirBloqCache, C);
                    //estCache[dirBloqCache][EST] = C;    // Estado del bloque que ocupa ahora esa direccion de cache
                break;
                case I:
                    // Previsto para los directorios, por el momento no puede estar invalido
                    // busco el dueño
                    // le pido que escriba a mem 
                    // lo pido para leer
                break;
            }
        }
        //regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
        //estr.signalC(myNumP);
        //regs[X] = dcache[dirBloqCache][numPalabra];     // Carga la palabra que se ocupa al registro
    }
    
    // Escribir una palabra
    //SW RX, n(RY)
    //M(n + (Ry))  Rx
    //Y X n
    public void SW(int Y, int X, int n){
        int numByte = regs[Y]+n;                                // Numero del byte que quiero leer de memoria 
        int numBloqMem = Math.floorDiv(numByte,16);             // Indice del bloque en memoria (0-24)
        int numPalabra = (numByte%16)/4;
        int dirBloqCache = numBloqMem%4;                        // Indice donde debe estar el bloque en cache
        int idBloqEnCache = getIdBloqueCache(dirBloqCache);         // ID del bloque que ocupa actualmente esa direccion en cache
        //int idBloqEnCache = estCache[dirBloqCache][ID];         // ID del bloque que ocupa actualmente esa direccion en cache
        int estadoBloqEnCache = getEstBloqueCache(dirBloqCache);  // Estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        //int estadoBloqEnCache = estCache[dirBloqCache][EST];    // Estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        int dirNumBloqMem = numBloqMem*4;
 
        //CASO 1: el bloque que requerimos no esta en cache, en su lugar hay otro bloque
        if(idBloqEnCache != dirNumBloqMem){
            if(idBloqEnCache == -1){
                cargarACache(dirNumBloqMem, dirBloqCache);
                setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                setEstBloqueCache(dirBloqCache, C);
                //estCache[dirBloqCache][ID] = dirNumBloqMem;     // Bloque que ocupa ahora esa direccion de cache
                //estCache[dirBloqCache][EST] = C;                // Estado del bloque que ocupa ahora esa direccion de cache
            }else{
                switch(estadoBloqEnCache){
                    case C:
                        // Nos traemos el bloque de memoria a cache
                        cargarACache(dirNumBloqMem, dirBloqCache);
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, C);
                        //estCache[dirBloqCache][ID] = dirNumBloqMem;     // Bloque que ocupa ahora esa direccion de cache
                        //estCache[dirBloqCache][EST] = C;                // Estado del bloque que ocupa ahora esa direccion de cache
                    break;
                    case M:
                        guardarEnMemoria(getIdBloqueCache(dirBloqCache), dirBloqCache);   // Creo que esos son los parámetros correctos. -Érick
                        //guardarEnMemoria(estCache[dirBloqCache][ID], dirBloqCache);   // Creo que esos son los parámetros correctos. -Érick
                        
                        setEstBloqueCache(dirBloqCache, C);
                        //estCache[dirBloqCache][EST] = C;
                        cargarACache(dirNumBloqMem, dirBloqCache);
                        
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, C);
                        //estCache[dirBloqCache][ID] = dirNumBloqMem;         // Bloque que ocupa ahora esa direccion de cache
                        //estCache[dirBloqCache][EST] = C;                    // Estado del bloque que ocupa ahora esa direccion de cache
                    break;
                    case I:
                        //cargarACache(dirNumBloqMem, dirBloqCache);
                        //estCache[dirBloqCache][ID] = dirNumBloqMem;     // Bloque que ocupa ahora esa direccion de cache
                        //estCache[dirBloqCache][EST] = C;                // Estado del bloque que ocupa ahora esa direccion de cache
                        //previsto para los directorios, por el momento no puede estar invalido
                    break;
                }
            }
        }else{  // HIT :D
            switch(estadoBloqEnCache){
                case C:

                break;
                case M:
                    guardarEnMemoria(dirNumBloqMem, dirBloqCache);
                    setEstBloqueCache(dirBloqCache, M);
                    //estCache[dirBloqCache][EST] = M;    // Estado del bloque que ocupa ahora esa direccion de cache
                break;
                case I:
                    //previsto para los directorios, por el momento no puede estar invalido
                break;
            }
        }
        setPalabraCache(dirBloqCache,numPalabra, regs[X]);
        //dcache[dirBloqCache][numPalabra] = regs[X];
        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
        setEstBloqueCache(dirBloqCache, M);
        //estCache[dirBloqCache][ID] = dirNumBloqMem; // Bloque que ocupa ahora esa direccion de cache
        //estCache[dirBloqCache][EST] = M;            // Estado del bloque que ocupa ahora esa direccion de cache
    }
    
    //RX, ETIQ
    //Si Rx = 0 SALTA
    //X 0 n
    public void BEQZ(int X, int n){
        if(regs[X]==0){
            if(n >= 0){
                PC+=4*(n-1);
            }
            else{
                PC+=4*(n);
            }
        }
    }
    
    //RX, ETIQ
    //Si Rx != 0 SALTA
    //X 0 n
    public void BNEZ(int X, int n){
        if(regs[X]!=0){
            //System.out.print("Si el R"+X+" es distinto de 0, brinco ");
            if(n >= 0){
                PC+=4*(n-1);
                //System.out.print(4*(n-1)+".\n");
            }
            else{
                //IR -> 12 
                //PC -> 16
                //quiero moverme -2 y llegar a 4
                //PC += 4*-2 = -8 -> PC = 12-8 = 8
                PC+=4*(n);
                //System.out.print(4*(n)+".\n");
            }
        }
    }
    
    //RX, RY, #n
    //Rx  (Ry) + n
    //Y X n
    public void DADDI(int Y, int X, int n){
        regs[X]=regs[Y]+n;
    }
    
    //RX, RY, RZ
    //Rx  (Ry) + (Rz)
    //Y Z X
    public void DADD(int Y, int Z, int X){
        regs[X]=regs[Y]+regs[Z];
    }
    
    //DSUB RX, RY, RZ
    //Rx  (Ry) - (Rz)
    //Y Z X
    //34 5 1 5
    public void DSUB(int Y, int Z, int X){
        regs[X]=regs[Y]-regs[Z];
    }
    
    public void FIN(){
        stop = 1;
    }
    
    //procesa una instruccion
    public void procesarInstruccion(int i){
        PC += 4;
        int cod, p1, p2, p3;
        cod = myMp.getInstIdx(i);
        p1 = myMp.getInstIdx(i+1);
        p2 = myMp.getInstIdx(i+2); 
        p3 = myMp.getInstIdx(i+3);
        //System.out.println("Mi cod es "+cod);
        switch(cod){
            case 8:
                DADDI(p1, p2, p3);
            break;
            case 32:
                DADD(p1, p2, p3);
            break;
            case 34:
                DSUB(p1, p2, p3);
            break;
            case 35:
                LW(p1, p2, p3);
            break;
            case 43:
                SW(p1, p2, p3);
            break;
            case 4:
                BEQZ(p1, p3);
            break;
            case 5:
                BNEZ(p1, p3);
            break;
            case 63:
                FIN();
            break;
        }
        puedoSeguir = true;
        try{
            myMp.barrera.await();
            ciclo++; cont++;
            //System.out.println("Ciclo #"+ciclo+". Puede cambiar de instrucción.\n");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /*
    public void procesar(int pcA, int limit){
        stop = 0;
        IR = PC = pcA;
        while(stop != 1 && IR < limit){
            IR = PC;
            if(puedoSeguir){
                procesarInstruccion(IR);
                verEstado();
            }
            
        }
    }*/
    
    //set a las variables necesarias para reconocer el programa q se esta corriendo
    public void setPcAyLimit(int pcActual, int limite){
        this.pcA = pcActual;
        this.limit = limite;
    }
    
    //procesa cuantas instrucciones tenga el programa
    public void procesar(){
        int cont = 0;
        myMp.setClock(ciclo);
        stop = 0;
        IR = PC = pcA;
        while(stop != 1 && IR < limit){
            IR = PC;
            if(puedoSeguir){
                procesarInstruccion(IR);
                if(stop == 1){ /*verEstado();*/ myMp.verEstadisticas(); }
            }            
        }
    }
    
    //metodo run del hilo, aqui se hace la coherencia con el hilo principal
    public void run() {
        //procesar();
        while(destruir == 0){
            stop = 0;
            System.out.println(limit);
            procesar();
            System.out.println("lo hice");
            synchronized(this){
                notify();
                stop = 0;
                try{
                    wait();
                }catch(InterruptedException e){
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    
    //set de sentinela para terminar el hilo q se esta corriendo
    public void salir() {
        destruir = 1;
    }
    
    //muestra el estado del procesador
    public String verEstado(){
        String estado = "";
        estado += "El PC es: "+ PC + "\n";
        estado += "El IR es: "+ IR + "\n";
        estado += "Los registros de procesador son:\n";
        for(int i = 0; i < 32; i++){
            estado += regs[i]+", ";
        }
        estado += "\n";
        estado += "La memoria cache contiene:\n";
        for(int i = 0; i < 4; i++){
            estado+="Bloque "+i+", estado: "+getEstBloqueCache(i)+", idBloque: "+getIdBloqueCache(i)+" --> ";
            //estado+="Bloque "+i+", estado: "+estCache[i][EST]+", idBloque: "+estCache[i][ID]+" --> ";
            for(int j= 0; j < 4; j++){
                 estado += getPalabraCache(i,j) + ", ";
                 //estado += dcache[i][j]+ ", ";
            }
            estado += "\n";
        }
        estado += "La memoria de datos contiene:\n";
        for(int i = 0; i < 32; i++){
            estado += getPalabraMem(i)+", ";
            //estado += dmem[i]+", ";
        }
        estado += "\n";
        estado += "La cantidad de ciclos que tardó el hilo es: "+cont+"\n";
        System.out.println(estado);
        return estado; 
    }
    
}
