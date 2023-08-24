
### Escuela Colombiana de Ingeniería
### Arquitecturas de Software - ARSW
## Ejercicio Introducción al paralelismo - Hilos - Caso BlackListSearch

### Santiago Arévalo Rojas
### Juan Felipe Sánchez Pérez


### Dependencias:
####   Lecturas:
*  [Threads in Java](http://beginnersbook.com/2013/03/java-threads/)  (Hasta 'Ending Threads')
*  [Threads vs Processes]( http://cs-fundamentals.com/tech-interview/java/differences-between-thread-and-process-in-java.php)

### Descripción
  **Este ejercicio contiene una introducción a la programación con hilos en Java, además de la aplicación a un caso concreto.**
  

**Parte I - Introducción a Hilos en Java**

<strong>
	
1. De acuerdo con lo revisado en las lecturas, complete las clases CountThread, para que las mismas definan el ciclo de vida de un hilo que imprima por pantalla los números entre A y B.
2. Complete el método __main__ de la clase CountMainThreads para que:
	1. Cree 3 hilos de tipo CountThread, asignándole al primero el intervalo [0..99], al segundo [99..199], y al tercero [200..299].
	2. Inicie los tres hilos con 'start()'.
	3. Ejecute y revise la salida por pantalla. 
	4. Cambie el incio con 'start()' por 'run()'. Cómo cambia la salida?, por qué?.

 </strong>

Al ejecutarlo con start() se evidencia el correcto paralelismo que se esperaba, en este caso encontramos que primero se ejecuta el secondThread, pero no imprime en orden los números:
	![img.png](img/img.png)

Al hacerlo con run(), se observa que se imprimen en orden los números:
	![img_1.png](img/img_1.png)

Esto sucede porque al hacerlo de la primera forma se hace un llamado al método run() de cada hilo creado, ejecutándose el contenido de este sobre el hilo principal (main()), mientras que con start() se ejecutan los tres hilos al tiempo junto con el hilo main, imprimiendo los números en un orden aleatorio debido a la concurrencia.

**Parte II - Ejercicio Black List Search**


<strong>

Para un software de vigilancia automática de seguridad informática se está desarrollando un componente encargado de validar las direcciones IP en varios miles de listas negras (de host maliciosos) conocidas, y reportar aquellas que existan en al menos cinco de dichas listas. 

Dicho componente está diseñado de acuerdo con el siguiente diagrama, donde:

- HostBlackListsDataSourceFacade es una clase que ofrece una 'fachada' para realizar consultas en cualquiera de las N listas negras registradas (método 'isInBlacklistServer'), y que permite también hacer un reporte a una base de datos local de cuando una dirección IP se considera peligrosa. Esta clase NO ES MODIFICABLE, pero se sabe que es 'Thread-Safe'.

- HostBlackListsValidator es una clase que ofrece el método 'checkHost', el cual, a través de la clase 'HostBlackListDataSourceFacade', valida en cada una de las listas negras un host determinado. En dicho método está considerada la política de que al encontrarse un HOST en al menos cinco listas negras, el mismo será registrado como 'no confiable', o como 'confiable' en caso contrario. Adicionalmente, retornará la lista de los números de las 'listas negras' en donde se encontró registrado el HOST.

![](img/Model.png)

Al usarse el módulo, la evidencia de que se hizo el registro como 'confiable' o 'no confiable' se dá por lo mensajes de LOGs:

INFO: HOST 205.24.34.55 Reported as trustworthy

INFO: HOST 205.24.34.55 Reported as NOT trustworthy


Al programa de prueba provisto (Main), le toma sólo algunos segundos análizar y reportar la dirección provista (200.24.34.55), ya que la misma está registrada más de cinco veces en los primeros servidores, por lo que no requiere recorrerlos todos. Sin embargo, hacer la búsqueda en casos donde NO hay reportes, o donde los mismos están dispersos en las miles de listas negras, toma bastante tiempo.

Éste, como cualquier método de búsqueda, puede verse como un problema [vergonzosamente paralelo](https://en.wikipedia.org/wiki/Embarrassingly_parallel), ya que no existen dependencias entre una partición del problema y otra.

Para 'refactorizar' este código, y hacer que explote la capacidad multi-núcleo de la CPU del equipo, realice lo siguiente:

1. Cree una clase de tipo Thread que represente el ciclo de vida de un hilo que haga la búsqueda de un segmento del conjunto de servidores disponibles. Agregue a dicha clase un método que permita 'preguntarle' a las instancias del mismo (los hilos) cuantas ocurrencias de servidores maliciosos ha encontrado o encontró.

2. Agregue al método 'checkHost' un parámetro entero N, correspondiente al número de hilos entre los que se va a realizar la búsqueda (recuerde tener en cuenta si N es par o impar!). Modifique el código de este método para que divida el espacio de búsqueda entre las N partes indicadas, y paralelice la búsqueda a través de N hilos. Haga que dicha función espere hasta que los N hilos terminen de resolver su respectivo sub-problema, agregue las ocurrencias encontradas por cada hilo a la lista que retorna el método, y entonces calcule (sumando el total de ocurrencuas encontradas por cada hilo) si el número de ocurrencias es mayor o igual a _BLACK_LIST_ALARM_COUNT_. Si se da este caso, al final se DEBE reportar el host como confiable o no confiable, y mostrar el listado con los números de las listas negras respectivas. Para lograr este comportamiento de 'espera' revise el método [join](https://docs.oracle.com/javase/tutorial/essential/concurrency/join.html) del API de concurrencia de Java. Tenga también en cuenta:

	* Dentro del método checkHost Se debe mantener el LOG que informa, antes de retornar el resultado, el número de listas negras revisadas VS. el número de listas negras total (línea 60). Se debe garantizar que dicha información sea verídica bajo el nuevo esquema de procesamiento en paralelo planteado.

	* Se sabe que el HOST 202.24.34.55 está reportado en listas negras de una forma más dispersa, y que el host 212.24.24.55 NO está en ninguna lista negra.

</strong>

La implementación se encuentra en el repositorio.


**Parte II.I Para discutir la próxima clase (NO para implementar aún)**

**La estrategia de paralelismo antes implementada es ineficiente en ciertos casos, pues la búsqueda se sigue realizando aún cuando los N hilos (en su conjunto) ya hayan encontrado el número mínimo de ocurrencias requeridas para reportar al servidor como malicioso. Cómo se podría modificar la implementación para minimizar el número de consultas en estos casos?, qué elemento nuevo traería esto al problema?**

Podría implementarse una forma en la que existan dos variables compartidas por todos los hilos, donde una contiene las ocurrencias de cada host en una lista negra, y otra  una lista en la que se almacena dónde se ha encontrado al servidor como malicioso. Cuando esta primera variable llega a 5, se haría que se detuviera la ejecución, y por lo tanto, se minimizaría el costo de los recursos usados para la realización del proceso de búsqueda. Esto implicaría el uso de variables que puedan ser accedidas y modificadas por todos los hilos, y una forma de evitar errores cuando dos o más hilos intenten modificarla al tiempo.

**Parte III - Evaluación de Desempeño**

**A partir de lo anterior, implemente la siguiente secuencia de experimentos para realizar las validación de direcciones IP dispersas (por ejemplo 202.24.34.55), tomando los tiempos de ejecución de los mismos (asegúrese de hacerlos en la misma máquina):**

1. **Un solo hilo.**
	
	La ejecución con un solo hilo es tan rápida que no fue posible seleccionar el programa en ejecución en VisualVM.

2. **Tantos hilos como núcleos de procesamiento (haga que el programa determine esto haciendo uso del [API Runtime](https://docs.oracle.com/javase/7/docs/api/java/lang/Runtime.html)).**

	Para tener tantos hilos como núcleos se hace por medio de la clase Runtime a través del método availableProcessors():
    ![img_2.png](img/img_2.png)
	
	En este caso se cuenta con 12 núcleos de procesamiento:
	![img_4.png](img/img_4.png)

	Se evidencia que el gasto de CPU es del 0,1%, el espacio de memoria usado es de 17'530.880B y un tiempo de ejecución de 10 segundos:
	![img_5.png](img/img_5.png)

3. **Tantos hilos como el doble de núcleos de procesamiento.**

	Para esta prueba se realiza de la misma manera que el índice 2, pero en este caso con 24 núcleos de procesamiento.

	Los resultados obtenidos indican que el gasto de CPU se mantiene igual, pero en cambio el espacio de memoria aumenta a 22'163.800B y el tiempo de ejecución disminuye a 4 segundos:
	![img_6.png](img/img_6.png)

4. **50 hilos.**

	En este caso el gasto de CPU es tan bajo que el monitoreo no lo muestra, por otro lado el espacio de memoria volvió a bajar a 13'550.176B y el tiempo sigue disminuyendo a 2 segundos:
	![img_7.png](img/img_7.png)
	
5. **100 hilos.**

	Finalmente tampoco detecta el gasto de CPU, el espacio en memoria aumenta y como se esperaba el tiempo de ejecución disminuye a 1 segundo:
	![img_8.png](img/img_8.png)

**Al iniciar el programa ejecute el monitor jVisualVM, y a medida que corran las pruebas, revise y anote el consumo de CPU y de memoria en cada caso.** ![](img/jvisualvm.png)

**Con lo anterior, y con los tiempos de ejecución dados, haga una gráfica de tiempo de solución vs. número de hilos. Analice y plantee hipótesis con su compañero para las siguientes preguntas (puede tener en cuenta lo reportado por jVisualVM):**

Se asume que el tiempo de ejecución con 1 hilo es de 0 segundos para graficar, con esto y con los datos obtenidos, se registra el siguiente comportamiento:  
![img_11.png](img/img_11.png)

![img_10.png](img/img_10.png)

Con esto concluimos gracias al log de la aplicación que con 1 hilo se demora menos porque es la cantidad con la que menos listas busca, exactamente realiza dicha búsqueda en 1.001 listas.

**Parte IV - Ejercicio Black List Search**

<strong>

1. Según la [ley de Amdahls](https://www.pugetsystems.com/labs/articles/Estimating-CPU-Performance-using-Amdahls-Law-619/#WhatisAmdahlsLaw?):

	![](img/ahmdahls.png), donde _S(n)_ es el mejoramiento teórico del desempeño, _P_ la fracción paralelizable del algoritmo, y _n_ el número de hilos, a mayor _n_, mayor debería ser dicha mejora. Por qué el mejor desempeño no se logra con los 500 hilos?, cómo se compara este desempeño cuando se usan 200?.

</strong>

Analizando de forma teórica y práctica, se prueba que con 500 hilos se logra un mejor rendimiento que con 200, pero no se lográ el mejor rendimiento os 500 hilos. Al suponer teóricamente que la fracción paralelizable es P=0.7, a partir de la ley de Amdahls se evidencia que con 500 hilos: S(500)=3.32, mientras que con 200 hilos: S(200)=3.29. Por otro lado, con el apoyo de VisualVM, se observa que con 500 hilos fue casi que instantánea la respuesta al problema, mientras que con 200 hilos de procesamiento se tomaba un poco más de tiempo para finalizar el proceso.

<strong>

2. Cómo se comporta la solución usando tantos hilos de procesamiento como núcleos comparado con el resultado de usar el doble de éste?.

</strong>

Cuando usamos tantos hilos de procesamiento como el doble de núcleos del dispositivo sobre el cual estamos trabajando, se observa a través de VisualVM una disminución notable de 6 segundos en el tiempo empleado para la realiazación del proceso, pero un aumento en la memoria de 5´000.000 B aproximadamente. Ahora, partiendo de la misma hipótesis anterior, teniendo como fracción paralelizable del algoritmo P=0.7, vemos que se obtiene una mayor mejora teórica en el desempeño cuando se usan tantos hilos como el doble de núcleos del procesador, que cuando se usan exactamente la misma cantidad de hilos que de núcleos: S(24)=3.04 y S(12)=2.79 respectivamente.

<strong>

3. De acuerdo con lo anterior, si para este problema en lugar de 100 hilos en una sola CPU se pudiera usar 1 hilo en cada una de 100 máquinas hipotéticas, la ley de Amdahls se aplicaría mejor?. Si en lugar de esto se usaran c hilos en 100/c máquinas distribuidas (siendo c es el número de núcleos de dichas máquinas), se mejoraría?. Explique su respuesta.

</strong>

En primer lugar, como vimos en la práctica, al usar sólamente un hilo, la velocidad con la que se ejecutó el programa fue tal que no se alcanzó a seleccionar el programa en ejecución en VisualVM. Por otro lado, analizando detalladamente la ley de Amdahls, si n=1, entonces S(1)=1, por lo que se mejoraría en factor de 1 el algoritmo con sólo un hilo en un dispositivo. Aprovechando el uso de múltiples dispositivos, despreciando el tiempo que les tomaría comunicarse la información entre ellos y el consumo de otros recursos como energía, sería bastante eficiente.  
Si se usarán por máquina tantos hilos como núcleos posean, podría ocurrir lo que sucedió anteriormente a través de VisualVM, donde se disminuyó notablemente el tiempo de ejecución, pero aumentó el costo de memoria que implicaba la búsqueda en las listas negras. Sin embargo, teóricamente y según la hipótesis planteada, entre más hilos se logra un aumento en la mejora del desempeño de ejecución del programa.

