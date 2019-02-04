# Prediction
1) Rename csv (`mv LeechUrl2.prediction.csv LeechUrl2.prediction.X.csv`, where X = n+1)
1) If necessary: stop job (find using `ps aux|grep Leech`)
1) `export GOOGLE_APPLICATION_CREDENTIALS=/home/markus_d_meier/sort-pics-9f5bbbe58175.json`
1) Restart job with param `cat LeechUrl2.prediction.*|wc -l`

#  Categorization
## Sample check
* Sample 300 random results
* Deduct a threshold so that 99% above are correct and 1% below can be handled manually (i.e. "not much")

## Auto sort algorithm
* Lines with category above threshold

## Manual handling
* Lines with error
* Lines with no category
* Lines with category below threshold