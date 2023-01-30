### Introducere

Pentru implementarea acestei teme, am realizat 2 taskuri, folosite de 2 pool-uri
diferite. Primul task se ocupa de de citirea comenzilor si numarului de produse din
orders_text, iar al doilea task se ocupa de citirea si validarea produselor din
order_products.txt 

### Flow si impelemtare

##### 1. Clasa Tema2:
Reprezinta entry-point-ul programului. Se ocupa de
validarea argumentelor de intrare, generarea BufferedReader necesar citirii din
fisierul orders.txt, crearea pool-ului (de nivel 1) si adagarea task-urilor
(de nivel 1) in pool. Acest pool este limitat, astfel incat sa avem maxim
thread-uri active date de numarul primit ca argument.

##### 2. Clasa Level1
Reprezinta task-ul realizat de thread-urile de nivel 1.
Acestea impart acelasi BufferedReader pentru citire. Pentru ca 2 thread-uri sa
nu citeasca aceiasi linie (race condition), citirea liniei este realizata in cadrul
unui bloc de sincronizare. Mai departe se extrage id-ul comenzii si numarul de
produse. Daca numarul de produse este 0, se omite aceasta comanda (se foloseste continue).
Mai departe, se introduc in pool-ul de nivel 2, un numar de task-uri
egal cu numarul de produse din comanda. Aceste task-uri primesc ca instantiere
id-ul comenzii, numarul de produse (dat ca atomic integer pentru a contoriza
cate produse au ramas din comanda necitite), un BufferedReader comun (doar intre task-urile cu acelasi id de comanda).
Thread-ul de nivel 1 ramane blocat pana cand
toate task-urile de nivel 2 create in cadrul acestuia au fost realizate. Dupa
deblocare, se task-ul scrie in orders_out.txt, faptul ca comanda a fost trimisa.
Dupa ce toate task-uirle de nivel 1 s-au incheiat, se inchid ambele pool-uri,
si se face flush la FileWriter. Pool-ul de nivel 1 nu este inchis pana cand
task-urile de nivel 2 nu s-au incheiat.

##### 3. Clasa Level2
Reprezinta task-ul realizat de thread-urile de nivel 2. Pentru
task-urile care folosesc acelasi BufferedReader pentru citirea din products_order.txt,
citirea unei linii este sincronizata folosind aceiasi idee ca la task-urile de
nivel 2. Daca linia citita are id-ul de comanda identic cu id-ul retinut la
initializare, se scrie sincronizat in order_products.txt faptul ca produsul a fost
trimis, si se decrementeaza variabila atomica care reprezinta numarul de produse
ramase din comanda curenta. Mai departe, thread-ul curent isi termina executia (se 
de break in while-ul principal) si face flush la FilreWriter daca este ultimul task
din pool.
Pool-ul de nivel 2 a fost initializat astfel incat sa nu poata fi executate
simultan mai mult de numarul maxim de thread-uri dat ca argument.
