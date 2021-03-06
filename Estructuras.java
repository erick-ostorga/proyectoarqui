package proy_arqui;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Estructuras
{     
    private final int ID = 0;
    private final int EST = 1;
    
    private final int B = 0;    // BLOQUE DIRECTORIO
    private final int E = 1;    // ESTADO DIRECTORIO
    private final int P1 = 2;   // PROCESADOR 1
    private final int P2 = 3;   // PROCESADOR 2
    private final int P3 = 4;   // PROCESADOR 3 
    
    private final int C = 0;    // COMPARTIDO
    private final int M = 1;    // MODIFICADO
    private final int I = 2;    // INVÁLIDO
    private final int U = 3;    // UNCACHED
    
    ////////////////////////////////////////////////////////////////////////////
    //MEMORIA COMPARTIDA
    //0-31|32-63|64-95
    // P1 | P2  | P3 
    private int smem[] = new int[96];    
    
    ////////////////////////////////////////////////////////////////////////////
    //DIRECTORIOS
    
    // B|E|P1|P2|P3
    // 8 BLOQUES
    private int dir1[][] = new int[8][5];
    
    // B|E|P1|P2|P3
    // 8 BLOQUES
    private int dir2[][] = new int[8][5];
    
    // B|E|P1|P2|P3
    // 8 BLOQUES
    private int dir3[][] = new int[8][5];
    
    //FIN DE LOS DIRECTORIOS
    ////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////
    //CACHES
 
    public int dcache1[][] = new int[4][4];     // Cache de datos (4 bloques, cada bloque con 4 palabras, cada palabra 4 bytes)
    public int estCache1[][] = new int[4][2];   // 8bloques*4 = 32 palabras ---> 32palabras*4 = 128 direcciones de palabras
    
    public int dcache2[][] = new int[4][4];     // Cache de datos (4 bloques, cada bloque con 4 palabras, cada palabra 4 bytes)
    public int estCache2[][] = new int[4][2];   // 8bloques*4 = 32 palabras ---> 32palabras*4 = 128 direcciones de palabras
    
    public int dcache3[][] = new int[4][4];     // Cache de datos (4 bloques, cada bloque con 4 palabras, cada palabra 4 bytes)
    public int estCache3[][] = new int[4][2];   // 8bloques*4 = 32 palabras ---> 32palabras*4 = 128 direcciones de palabras

    //FIN DE LAS CACHES
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    //SEMÁFOROS
    
    Semaphore SemaforoDir1 = new Semaphore(1);
    Semaphore SemaforoDir2 = new Semaphore(1);
    Semaphore SemaforoDir3 = new Semaphore(1);
    Semaphore SemaforoCache1 = new Semaphore(1);
    Semaphore SemaforoCache2 = new Semaphore(1);
    Semaphore SemaforoCache3 = new Semaphore(1);
    
    //FIN DE SEMÁFOROS
    ////////////////////////////////////////////////////////////////////////////
    
    // CONSTRUCTOR DE CLASE ESTRUCTURAS
    public Estructuras()
    {
        inicializarDirs();
        inicializarCaches();
    }
    
    public void inicializarDirs()
    {
        for(int i = 0; i < 8; i++)
        {
            dir1[i][B] = (i*4);
            dir1[i][E] = U;
            dir2[i][B] = 32+(i*4);
            dir2[i][E] = U;
            dir3[i][B] = 64+(i*4);
            dir3[i][E] = U;
        }
    }
    
    public void inicializarCaches()
    {
        for(int i =0; i < 4; ++i)
        {
            setEstBloqueCache(1, i, I);
            setIdBloqueCache(1, i, -1);
            setEstBloqueCache(2, i, I);
            setIdBloqueCache(2, i, -1);
            setEstBloqueCache(3, i, I);
            setIdBloqueCache(3, i, -1);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //USO DE LOS SEMÁFOROS
    
    public void waitD(int numDir){
        switch(numDir){
            case 1:
            {
                try {
                    SemaforoDir1.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 2:
            {
                try {
                    SemaforoDir2.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 3:
            {
                try {
                    SemaforoDir3.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
        }
    }
    
    public void signalD(int numDir){
        switch(numDir){
            case 1:
            {
                try {
                    SemaforoDir1.release();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 2:
            {
                try {
                    SemaforoDir2.release();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 3:
            {
                try {
                    SemaforoDir3.release();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
        }
    }
    
    public int disponibleD(int numDir){
        int result = 0;
        switch(numDir){
            case 1:
            {
                try {
                    result = SemaforoDir1.availablePermits();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 2:
            {
                try {
                    result = SemaforoDir2.availablePermits();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 3:
            {
                try {
                    result = SemaforoDir3.availablePermits();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
        }
        return result;
    }
    
    public void waitC(int numCach){
        switch(numCach){
            case 1:
            {
                try {
                    SemaforoCache1.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 2:
            {
                try {
                    SemaforoCache2.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 3:
            {
                try {
                    SemaforoCache3.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
        }
    }
    
    public void signalC(int numCach){
        switch(numCach){
            case 1:
            {
                try {
                    SemaforoCache1.release();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 2:
            {
                try {
                    SemaforoCache2.release();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 3:
            {
                try {
                    SemaforoCache3.release();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
        }
    }
    
    public int disponibleC(int numCach){
        int result = 0;
        switch(numCach){
            case 1:
            {
                try {
                    result = SemaforoCache1.availablePermits();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 2:
            {
                try {
                    result = SemaforoCache2.availablePermits();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 3:
            {
                try {
                    result = SemaforoCache3.availablePermits();
                } catch (Exception ex) {
                    Logger.getLogger(Estructuras.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
        }
        return result;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // SECCIÓN DE SETS Y GETS, NO IMPORTA DE DONDE VENGA LA MEMORIA Y CACHE,
    // LOS CAMBIOS SE HACEN SOLO ACÁ Y NO EN EL RESTO DEL CÓDIGO.
    
    // recibe indiceBloque = indice del bloque en cache de 0 a 3
    // int indicePalabra = indice de palabra en cache de 0 a 3
    // nuevo valor para esa posición de caché
    public void setPalabraCache(int numCache, int indiceBloque, int indicePalabra, int valor)
    {
        switch(numCache)
        {
            case 1:
                dcache1[indiceBloque][indicePalabra] = valor;
            break;
            case 2:
                dcache2[indiceBloque][indicePalabra] = valor;
            break;
            case 3:
                dcache3[indiceBloque][indicePalabra] = valor;
            break;
        }
    }
    
    // recibe indiceBloque = indice del bloque en cache de 0 a 3
    // int indicePalabra = indice de palabra en cache de 0 a 3
    public int getPalabraCache(int numCache, int indiceBloque, int indicePalabra)
    {
        int palabra = -1;
        switch(numCache)
        {
            case 1:
                palabra = dcache1[indiceBloque][indicePalabra];
            break;
            case 2:
                palabra = dcache2[indiceBloque][indicePalabra];
            break;
            case 3:
                palabra = dcache3[indiceBloque][indicePalabra];
            break;
        }
        return palabra; 
    }
    
    // recibe indiceBloque = indice del bloque en cache de 0 a 3
    // nuevo valor de estado
    public void setEstBloqueCache(int numCache, int indiceBloque, int estado)
    {
        switch(numCache)
         {
            case 1:
                estCache1[indiceBloque][EST] = estado;
            break;
            case 2:
                estCache2[indiceBloque][EST] = estado;
            break;
            case 3:
                estCache3[indiceBloque][EST] = estado;
            break;
        }
    }
    
    // recibe indiceBloque = indice del bloque en cache de 0 a 3
    public int getEstBloqueCache(int numCache, int indiceBloque)
    {
        int estado = -1;
        switch(numCache)
        {
            case 1:
                estado = estCache1[indiceBloque][EST];
            break;
            case 2:
                estado = estCache2[indiceBloque][EST];
            break;
            case 3:
                estado = estCache3[indiceBloque][EST];
            break;
        }
        return estado;
    }
    
    // recibe indiceBloque = indice del bloque en cache de 0 a 3
    // nuevo id para esa posición de caché
    public void setIdBloqueCache(int numCache, int indiceBloque, int id)
    {
        switch(numCache)
        {
            case 1:
                estCache1[indiceBloque][ID] = id;
            break;
            case 2:
                estCache2[indiceBloque][ID] = id;
            break;
            case 3:
                estCache3[indiceBloque][ID] = id;
            break;
        }
    }
    
    // recibe indiceBloque = indice del bloque en cache de 0 a 3
    public int getIdBloqueCache(int numCache, int indiceBloque)
    {
        int id = -1;
        switch(numCache)
        {
            case 1:
                id = estCache1[indiceBloque][ID];
            break;
            case 2:
                id = estCache2[indiceBloque][ID];
            break;
            case 3:
                id = estCache3[indiceBloque][ID];
            break;
        }
        return id;
    }
    
    //recibe indiceMem = indice en memoria (0-95)
    public void setPalabraMem(int numCache, int indiceMem, int valor)
    {
        smem[indiceMem] = valor;
    }
    
    //recibe indiceMem = indice en memoria (0-95)
    public int getPalabraMem(int numCache, int indiceMem)
    {
        return smem [indiceMem];
    }
    
    // recibe indiceDir = indice del 0 al 7
    // constante de entrada
    // nuevo valor
    public void setEntradaDir(int numDir, int indiceDir, int entrada, int valor)
    {
        switch(numDir)
        {
            case 1:
                dir1[indiceDir][entrada] = valor;
            break;
            case 2:
                dir2[indiceDir][entrada] = valor;
            break;
            case 3:
                dir3[indiceDir][entrada] = valor;
            break;
        }
    }
    
    // recibe indiceDir = indice del 0 al 7
    // constante de entrada
    public int getEntradaDir(int numDir, int indiceDir, int entrada)
    {
        int salida = -1;
        switch(numDir)
        {
            case 1:
                salida = dir1[indiceDir][entrada];
            break;
            case 2:
                salida = dir2[indiceDir][entrada];
            break;
            case 3:
                salida = dir3[indiceDir][entrada];
            break;
        }
        return salida;
    }
    
    //FIN DE LA SECCION DE SETS Y GETS
    ////////////////////////////////////////////////////////////////////////////
    
    // ESTE METODO DEVUELVE EL ESTADO EN EL DIRECTORIO DE UN BLOQUE CON ID = DIR MEM BLOQUE (0-23)
    // DEVUELVE: 'C', 'M', 'U'
    public int getEstadoBloqueDir(int idBloque)
    {
        int indiceDir, estado = -1;
        indiceDir = idBloque % 8;
        if(idBloque >= 0 && idBloque <=7)
        {
            //indiceDir = idBloque / 4;
            estado = getEntradaDir(1,indiceDir,E);
        }
        if(idBloque >= 8 && idBloque <=15)
        {
            //indiceDir = (idBloque-32)/4;
            estado = getEntradaDir(2,indiceDir,E);
        }
        if(idBloque >= 16 && idBloque <=23)
        {
            //indiceDir = (idBloque-64)/4;
            estado = getEntradaDir(3,indiceDir,E);
        }
        return estado;
    }
    
    // ESTE MÉTODO DEVUELVE EL DIRECTORIO CASA DE UN BLOQUE DE MEMORIA
    // REQUIERE: EL ID DEL BLOQUE EN MEMORIA
    // DEVUELVE: 1, 2, 3
    public int directorioPapa(int idBloque)
    {
        int papa = -1; 
        if(idBloque >= 0 && idBloque <=31)
        {
            papa = 1;
        }
        if(idBloque >= 32 && idBloque <=63)
        {
            papa = 2;
        }
        if(idBloque >= 64 && idBloque <=95)
        {
            papa = 3;
        }
        return papa;
    }
    
    public int mapearABloque(int indice){
        int bloque = -1;
        if(indice >= 0 && indice <=31){
            switch(indice){
                case 0: case 1: case 2: case 3: bloque = 0; break;
                case 4: case 5: case 6: case 7: bloque = 1; break;
                case 8: case 9: case 10: case 11: bloque = 2; break;
                case 12: case 13: case 14: case 15: bloque = 3; break;
                case 16: case 17: case 18: case 19: bloque = 4; break;
                case 20: case 21: case 22: case 23: bloque = 5; break;
                case 24: case 25: case 26: case 27: bloque = 6; break;
                case 28: case 29: case 30: case 31: bloque = 7; break;
            }
        }
        if(indice >= 32 && indice <=63){
            switch(indice){
                case 32: case 33: case 34: case 35: bloque = 8; break;
                case 36: case 37: case 38: case 39: bloque = 9; break;
                case 40: case 41: case 42: case 43: bloque = 10; break;
                case 44: case 45: case 46: case 47: bloque = 11; break;
                case 48: case 49: case 50: case 51: bloque = 12; break;
                case 52: case 53: case 54: case 55: bloque = 13; break;
                case 56: case 57: case 58: case 59: bloque = 14; break;
                case 60: case 61: case 62: case 63: bloque = 15; break;
            }
        }
        if(indice >= 64 && indice <=95){
            switch(indice){
                case 64: case 65: case 66: case 67: bloque = 16; break;
                case 68: case 69: case 70: case 71: bloque = 17; break;
                case 72: case 73: case 74: case 75: bloque = 18; break;
                case 76: case 77: case 78: case 79: bloque = 19; break;
                case 80: case 81: case 82: case 83: bloque = 20; break;
                case 84: case 85: case 86: case 87: bloque = 21; break;
                case 88: case 89: case 90: case 91: bloque = 22; break;
                case 92: case 93: case 94: case 95: bloque = 23; break;
            }
        }
        return bloque;
    }

    
    // idBloque = EL ID de la palabra en memoria (0-95)
    // ESTE MÉTODO DEVUELVE CUÁL PROCESADOR TIENE EL BLOQUE EN CASO DE QUE ESTÉ MODIFICADO
    // DEVUELVE; 1, 2, 3
    public int consultarDuenoBloqueDir(int idBloque)
    {
        int dueno = -1;
        int indiceDir;
        
        if(idBloque >= 0 && idBloque <=31)
        {
            indiceDir = mapearABloque(idBloque)%8;
            if(getEntradaDir(1,indiceDir,E) == M)
            {
                if(getEntradaDir(1,indiceDir,P1) == 1) dueno = 1;
                else if(getEntradaDir(1,indiceDir,P2) == 1) dueno = 2;
                else if(getEntradaDir(1,indiceDir,P3) == 1) dueno = 3;
            }
        }
        
        if(idBloque >= 32 && idBloque <=63)
        {
             indiceDir = mapearABloque(idBloque)%8;
            if(getEntradaDir(2,indiceDir,E)== M)
            {
                if(getEntradaDir(2,indiceDir,P1)== 1) dueno = 1;
                else if(getEntradaDir(2,indiceDir,P2)== 1) dueno = 2;
                else if(getEntradaDir(2,indiceDir,P3)== 1) dueno = 3;
            }
        }
        if(idBloque >= 64 && idBloque <=95)
        {
            indiceDir = mapearABloque(idBloque)%8;
            if(getEntradaDir(3,indiceDir,E)== M)
            {
                if(getEntradaDir(3,indiceDir,P1)== 1) dueno = 1;
                else if(getEntradaDir(3,indiceDir,P2)== 1) dueno = 2;
                else if(getEntradaDir(3,indiceDir,P3)== 1) dueno = 3;
            }
        }
        return dueno;
    }
    
    public void cargarACache(int numCache, int direccionMemoria, int direccionCache)
    {
        int j = direccionMemoria;
        for(int i = 0; i < 4; i++)
        {
            setPalabraCache(numCache, direccionCache, i, getPalabraMem(numCache, j));
            j++;
        }
    }
    
    public void guardarEnMemoria(int numCache, int direccionMemoria, int direccionCache)
    {
        int j = direccionMemoria;
        for(int i = 0; i < 4; i++)
        {
            setPalabraMem(numCache, j, getPalabraCache(numCache, direccionCache, i));
            j++;
        }
     }
   
    // ESTE MÉTODO RECIBE EL DIRECTORIO DUEÑO, PROCESADOR, ID BLOQUE
    // LO QUE HACE ES PONER PARA EL INDICE BLOQUE EN EL DIRECTORIO, 
    // PONE UN 1 EN LA COLUMNA CORRESPONDIENTE A PROCESADOR.
    public void anadirProcesador(int idBloque, int proce)
    {
        int indiceDir; 
        if(idBloque >= 0 && idBloque <=31)
        {
            //indiceDir = idBloque/4;
            indiceDir = mapearABloque(idBloque)%8;
            if(proce == 1) setEntradaDir(1, indiceDir, P1, 1);
            if(proce == 2) setEntradaDir(1, indiceDir, P2, 1);
            if(proce == 3) setEntradaDir(1, indiceDir, P3, 1);
        }
        if(idBloque >= 32 && idBloque <=63)
        {
            //indiceDir = (idBloque-32)/4;
            indiceDir = mapearABloque(idBloque)%8;
            if(proce == 1) setEntradaDir(2, indiceDir, P1, 1);
            if(proce == 2) setEntradaDir(2, indiceDir, P2, 1);
            if(proce == 3) setEntradaDir(2, indiceDir, P3, 1);
        }
        if(idBloque >= 64 && idBloque <=95)
        {
            //indiceDir = (idBloque-64)/4;
            indiceDir = mapearABloque(idBloque)%8;
            if(proce == 1) setEntradaDir(3, indiceDir, P1, 1);
            if(proce == 2) setEntradaDir(3, indiceDir, P2, 1);
            if(proce == 3) setEntradaDir(3, indiceDir, P3, 1);
        }
    }
    
    public void quitarProcesador(int idBloque, int proce)
    {
        int indiceDir; 
        if(idBloque >= 0 && idBloque <=31)
        {
            //indiceDir = idBloque/4;
            indiceDir = mapearABloque(idBloque)%8;
            if(proce == 1) setEntradaDir(1, indiceDir, P1, 0);
            if(proce == 2) setEntradaDir(1, indiceDir, P2, 0);
            if(proce == 3) setEntradaDir(1, indiceDir, P3, 0);
        }
        if(idBloque >= 32 && idBloque <=63)
        {
            //indiceDir = (idBloque-32)/4;
            indiceDir = mapearABloque(idBloque)%8;
            if(proce == 1) setEntradaDir(2, indiceDir, P1, 0);
            if(proce == 2) setEntradaDir(2, indiceDir, P2, 0);
            if(proce == 3) setEntradaDir(2, indiceDir, P3, 0);
        }
        if(idBloque >= 64 && idBloque <=95)
        {
            //indiceDir = (idBloque-64)/4;
            indiceDir = mapearABloque(idBloque)%8;
            if(proce == 1) setEntradaDir(3, indiceDir, P1, 0);
            if(proce == 2) setEntradaDir(3, indiceDir, P2, 0);
            if(proce == 3) setEntradaDir(3, indiceDir, P3, 0);
        }
    }
    
    public void verificarUncached(int idBloque)
    {
        int indiceDir;
        
        if(idBloque >= 0 && idBloque <=31)
        {
            //indiceDir = idBloque/4;
            indiceDir = mapearABloque(idBloque)%8;
            if(getEntradaDir(1, indiceDir, P1) == 0 && getEntradaDir(1, indiceDir, P2) == 0 && getEntradaDir(1, indiceDir, P3) == 0)
            {
                setEntradaDir(1, indiceDir, E, U); 
            }
        }
        
        if(idBloque >= 32 && idBloque <=63)
        {
            //indiceDir = (idBloque-32)/4;
            indiceDir = mapearABloque(idBloque)%8;
            if(getEntradaDir(2, indiceDir, P1) == 0 && getEntradaDir(2, indiceDir, P2) == 0 && getEntradaDir(2, indiceDir, P3) == 0)
            {
                setEntradaDir(2, indiceDir, E, U); 
            }
        }
        
        if(idBloque >= 64 && idBloque <=95)
        {
            //indiceDir = (idBloque-64)/4;
            indiceDir = mapearABloque(idBloque)%8;
            if(getEntradaDir(3, indiceDir, P1) == 0 && getEntradaDir(3, indiceDir, P2) == 0 && getEntradaDir(3, indiceDir, P3) == 0)
            {
                setEntradaDir(3, indiceDir, E, U);
            } 
        }
    }
    
    public void quitarCompartidos(int idBloque, int proc)
    {
        int indiceDir; 
        
        if((idBloque >= 0) && (idBloque <= 31))
        {
            //indiceDir = idBloque/4;
            indiceDir = mapearABloque(idBloque)%8;
            
            if((proc != 1) && (getEntradaDir(1, indiceDir, P1) == 1))
            {
                setEntradaDir(1, indiceDir, P1, 0);
                waitC(1);
                setEstBloqueCache(1, (idBloque%4), I);
                signalC(1);
            }
            if((proc != 2) && (getEntradaDir(1, indiceDir, P2) == 1))
            {
                setEntradaDir(1, indiceDir, P2, 0);
                waitC(2);
                setEstBloqueCache(2, (idBloque%4), I);
                signalC(2);
            }
            if((proc != 3) && (getEntradaDir(1, indiceDir, P3) == 1))
            {
                setEntradaDir(1, indiceDir, P3, 0);
                waitC(3);
                setEstBloqueCache(3, (idBloque%4), I);
                signalC(3);
            }
        }
        
        if((idBloque >= 32) && (idBloque <=63))
        {
            //indiceDir = (idBloque-32)/4;
            indiceDir = mapearABloque(idBloque)%8;
            
            if((proc != 1) && (getEntradaDir(2, indiceDir, P1) == 1))
            {
                setEntradaDir(2, indiceDir, P1, 0);
                waitC(1);
                setEstBloqueCache(1, (idBloque%4), I);
                signalC(1);
            }
            if((proc != 2) && (getEntradaDir(2, indiceDir, P2) == 1))
            {
                setEntradaDir(2, indiceDir, P2, 0);
                waitC(2);
                setEstBloqueCache(2, (idBloque%4), I);
                signalC(2);
            }
            if((proc != 3) && (getEntradaDir(2, indiceDir, P3) == 1))
            {
                setEntradaDir(2, indiceDir, P3, 0);
                waitC(3);
                setEstBloqueCache(3, (idBloque%4), I);
                signalC(3);
            }
        }
        
        if((idBloque >= 64) && (idBloque <=95))
        {
            //indiceDir = (idBloque-64)/4;
            indiceDir = mapearABloque(idBloque)%8;
            
            if((proc != 1) && (getEntradaDir(3, indiceDir, P1) == 1))
            {
                setEntradaDir(3, indiceDir, P1, 0);
                waitC(1);
                setEstBloqueCache(1, (idBloque%4), I);
                signalC(1);
            }
            if((proc != 2) && (getEntradaDir(3, indiceDir, P2) == 1))
            {
                setEntradaDir(3, indiceDir, P2, 0);
                waitC(2);
                setEstBloqueCache(2, (idBloque%4), I);
                signalC(2);
            }
            if((proc != 3) && (getEntradaDir(3, indiceDir, P3) == 1))
            {
                setEntradaDir(3, indiceDir, P3, 0);
                waitC(3);
                setEstBloqueCache(3, (idBloque%4), I);
                signalC(3);
            }
        }
    }

// FIN DE LA CLASE
}
