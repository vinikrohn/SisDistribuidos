import threading
import random
import string

resultados = [None, None]

def limpar_nomes(sublista, indice):
    lista_limpa = []
    for nome in sublista:
        nome_limpo = nome.strip().upper()
        lista_limpa.append(nome_limpo)
    resultados[indice] = lista_limpa

# Gera lista com 5.000 nomes de usuários (com espaços extras e minusculas, pra simular sujeira)
lista_nomes = []
for i in range(5000):
    nome = "  usuario" + str(i) + "  "
    lista_nomes.append(nome)

# Divide a lista em 2 blocos
metade = len(lista_nomes) // 2
bloco_a = lista_nomes[0:metade]
bloco_b = lista_nomes[metade:]

# Cria as threads
thread_a = threading.Thread(target=limpar_nomes, args=(bloco_a, 0))
thread_b = threading.Thread(target=limpar_nomes, args=(bloco_b, 1))

# Inicia as threads
thread_a.start()
thread_b.start()

# Aguarda o fim das threads
thread_a.join()
thread_b.join()

# Junta as duas listas resultantes
lista_final = resultados[0] + resultados[1]

print("Quantidade de nomes processados:", len(lista_final))
print("Primeiros 5 nomes limpos:", lista_final[:5])
