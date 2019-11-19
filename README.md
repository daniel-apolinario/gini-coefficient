# gini-coefficient
Microsserviço Java (Spring Boot) para cálculo do Coeficiente Gini

A fórmula para o cálculo está descrita abaixo:

<a href="https://www.codecogs.com/eqnedit.php?latex=G&space;=&space;\frac{\sum_{i=1}^{n}(2i-n-1)x_i}{n\sum_{i=1}^{n}x_i}" target="_blank"><img src="https://latex.codecogs.com/gif.latex?G&space;=&space;\frac{\sum_{i=1}^{n}(2i-n-1)x_i}{n\sum_{i=1}^{n}x_i}" title="G = \frac{\sum_{i=1}^{n}(2i-n-1)x_i}{n\sum_{i=1}^{n}x_i}" /></a>

O serviço recebe uma lista de valores (números) separados por vírgula e retorna um json que contém o valor calculado do coeficiente Gini.
