#
# Custom DSL
#
# author: ddoyle@redhat.com
#

[when][]There is a SimpleFact=SimpleFact()
[when][]-with id "{factId}"=id=="{factId}"

[then][]Print "{message}"=System.out.println("{message}");