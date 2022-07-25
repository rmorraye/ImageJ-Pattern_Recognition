# Tutorial: ImageJ - Plugin (Zernike - KNearest) #

*Passos para executar o Plugin*

Input: Apenas imagens 8-bit (ou seja, com apenas 1 canal de cor, tons de cinza, de 0 a 255 "tonalidades")

1º Passo:
 Baixe o software ImageJ (https://imagej.nih.gov/ij/download.html)

2º Passo:
 Coloque todos os arquivos *.java e *.class na pasta Filters (ImageJ\plugins\Filters) do diretório de instalação do ImageJ

3º Passo: 
 Abra o ImageJ e clique em "File" > "Open..." e abra uma imagem de referência (de preferência extraia as imagens do brodatz-gif.zip e selecione uma imagem)

4º Passo:
 Clique em "Plugins" > "Compile and Run..." e selecione o arquivo "K_Nearest.java"

5º Passo: 
 Abra o diretorio do banco de imagens (selecione qualquer imagem dentro do diretório)

6º Passo:
 Coloque os graus de Zernike (recomendado: 2 ~ 8)

7º Passo:
 Coloque a quantidade de vizinhos mais próximos(KNearest) e a qual função distância queira utilizar(1, 2 ou 3)


Obs.: Caso sua imagem de referência esteja no mesmo diretório do banco de imagens ela será a primeira a aparecer nos resultados

PS.: Após rodar e compilar o programa estára salvo em "Plugins" > "Filters" > "K-Nearest" não sendo necessário compilar novamente, a não ser que altere o código.
