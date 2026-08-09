import threading
import random

resultados = [0, 0, 0, 0]

def somar_sublista(sublista, indice):
    resultados[indice] = sum(sublista)

# Gera lista com 10.000 números aleatórios
lista = [random.randint(1, 100) for _ in range(10000)]

# Divide a lista em 4 partes iguais
tamanho_parte = len(lista) // 4
parte1 = lista[0:tamanho_parte]
parte2 = lista[tamanho_parte:tamanho_parte*2]
parte3 = lista[tamanho_parte*2:tamanho_parte*3]
parte4 = lista[tamanho_parte*3:tamanho_parte*4]

# Cria as 4 threads
t1 = threading.Thread(target=somar_sublista, args=(parte1, 0))
t2 = threading.Thread(target=somar_sublista, args=(parte2, 1))
t3 = threading.Thread(target=somar_sublista, args=(parte3, 2))
t4 = threading.Thread(target=somar_sublista, args=(parte4, 3))

# Inicia as threads
t1.start()
t2.start()
t3.start()
t4.start()

# Aguarda o fim das 4 threads
t1.join()
t2.join()
t3.join()
t4.join()

# Soma total
soma_total = sum(resultados)

print("Soma parcial 1:", resultados[0])
print("Soma parcial 2:", resultados[1])
print("Soma parcial 3:", resultados[2])
print("Soma parcial 4:", resultados[3])
print("Soma total:", soma_total)
