package fuoco;

public class Matrix
{
    double[][] values;
    int row;
    int col;

    Matrix( double[][] values ){
        this.values = values;
        this.row = values.length;
        this.col = values[0].length;
    }

    public Matrix Add( Matrix mx ){
        if ( row == mx.row && col == mx.col ){
            for ( int i=0; i<row; i++ )
                for ( int j=0; j<col; j++ )
                    values[i][j] = values[i][j] + mx.values[i][j];
        }
        return new Matrix(values);
    }

    private static double[] ArrayAdd ( double[] array, double [] array2 ){
        double[] newArray = new double[array.length];

        for ( int i=0; i<array.length; i++ )
            newArray[i] = array[i] + array2[i];

        return newArray;
    }

    public static String ArrayToString ( double[] array ){
        String s = "";

        for ( int i=0; i<array.length; i++ )
            s += array[i] + " ";

        return s;
    }

    public Matrix GaussReduction (){
        double[][] newValues = values.clone();

        for ( int i=0; i<row; i++ ){
            boolean flagS = false;

            for ( int j=0; j<row; j++ ){
                if ( newValues[i][j] != 0 ){
                    double[] temp = newValues[i];
                    newValues[i] = newValues[j];
                    newValues[j] = temp;
                    flagS = true;
                    break;
                }
            }

            if ( flagS ){
                for ( int j=i+1; j<row; j++ ){
                    double x = -1*newValues[j][i]/newValues[i][i];
                    newValues[j] = ArrayAdd( newValues[j] , ScalarMultiply( newValues[i] , x ) );
                }
            }
        }

        return new Matrix ( newValues );
    }

    public Matrix GetCol( int n ){
        double[][] colN = new double[row][1];

        for ( int i=0; i<row; i++ )
            colN[i][0] = values[i][n];

        return new Matrix ( colN );
    }

    public double GetDeterminant (){
        if ( col == row ){
            return GetDeterminantR( new Matrix ( values ) );
        }else{
            return Double.NaN;
        }
    }

    private double GetDeterminantR ( Matrix mx ){
        if ( mx.row == 1 )
            return mx.values[0][0];

        double part = 0;

        for( int i=0; i<mx.row; i++ ){
            double[][] newMx = new double[mx.row-1][mx.row-1];

            for ( int y=1; y<mx.row; y++ ){
                int ptr = 0;
                for ( int x=0; x<mx.row; x++ ){
                    if ( x==i ) continue;
                    newMx[y-1][ptr] = mx.values[y][x];
                    ptr++;
                }
            }

            if ( i%2 == 0 )
                part += mx.values[0][i] * GetDeterminantR( new Matrix (newMx) );
            else
                part -= mx.values[0][i] * GetDeterminantR( new Matrix (newMx) );
        }
        return part;
    }

    public Matrix GetRow( int n ){
        double[][] rowN = new double[1][col];
        System.arraycopy(values[n], 0, rowN[0], 0, col);

        return new Matrix ( rowN );
    }

    public static Matrix Identity ( int n ){
        double[][] newValues = new double[n][n];

        for ( int i=0; i<n; i++ )
            for ( int j=0; j<n; j++ )
                if ( i == j ) newValues[i][j] = 1;
                else newValues[i][j] = 0;

        return new Matrix ( newValues );
    }

    public Matrix Inverse (){
        if ( this.GetDeterminant() == 0 )
            return null;

        double[][] newValues = new double[row][row*2];

        for ( int i=0; i<row; i++ ){
            for ( int j=0; j<row*2; j++ ){
                if ( j<row ){
                    newValues[i][j] = values[i][j];
                }else{
                    if ( j-i == 4 ){
                        newValues[i][j] = 1;
                    }else{
                        newValues[i][j] = 0;
                    }
                }
            }
        }

        Matrix mx = new Matrix(newValues);
        mx = mx.GaussReduction();
        mx = mx.InverseGaussReduction();
        newValues = new double[row][row];

        for ( int i=0; i<row; i++ ){
            for ( int j=0; j<row; j++ ){
                newValues[i][j] = mx.values[i][j+row]/mx.values[i][i];
            }
        }

        return new Matrix(newValues);
    }

    public Matrix InverseGaussReduction(){
        double[][] newValues = values.clone();

        for ( int i=row-1; i>=0; i-- ){
            boolean flagS = false;

            for ( int j=row-1; j>=0; j-- ){
                if ( newValues[i][j] != 0 ){
                    double[] temp = newValues[i];
                    newValues[i] = newValues[j];
                    newValues[j] = temp;
                    flagS = true;
                    break;
                }
            }

            if ( flagS ){
                for ( int j=i-1; j>=0; j-- ){
                    double x = -1*newValues[j][i]/newValues[i][i];
                    newValues[j] = ArrayAdd( newValues[j] , ScalarMultiply( newValues[i] , x ) );
                }
            }
        }

        return new Matrix ( newValues );
    }

    public Matrix Multiply( Matrix mx ){
        double[][] newValues = null;

        if ( col == mx.row ){
            newValues = new double[row][mx.col];

            for ( int i=0; i<row; i++ )
                for ( int j=0; j<mx.col; j++ )
                    for ( int k=0; k<col; k++ )
                        newValues[i][j] += this.values[i][k] * mx.values[k][j];

        }
        return new Matrix(newValues);
    }

    public static double Norm ( double[] x ){
        return Math.sqrt( ScalarProduct(x,x) );
    }

    private static double[] ScalarMultiply ( double[] array, double x ){
        double[] newArray = new double[array.length];

        for ( int i=0; i<array.length; i++ )
            newArray[i] = array[i] * x;

        return newArray;
    }

    public Matrix ScalarMultiply ( double x ){
        double[][] newValues = new double[row][col];

        for ( int i=0; i<row; i++ )
            for ( int j=0; j<col; j++ )
                newValues[i][j] = x * newValues[i][j];

        return new Matrix ( newValues );
    }

    public static double ScalarProduct( double[] x ,  double[] y ){
        int r = 0;
        for ( int i=0; i<x.length; i++ )
            r += x[i]*y[i];

        return r;
    }

    public Matrix Subtract( Matrix mx ){
        if ( row == mx.row && col == mx.col ){
            for ( int i=0; i<row; i++ )
                for ( int j=0; j<col; j++ )
                    values[i][j] = values[i][j] - mx.values[i][j];

        }
        return new Matrix(values);
    }

    public Matrix Transpose(){
        double[][] newValues = new double[col][row];

        for ( int i=0; i<row; i++ ){
            for ( int j=0; j<col; j++ ){
                newValues[j][i] = values[i][j];
            }
        }

        return new Matrix ( newValues );
    }

    @Override
    public String toString(){
        String s = "";

        for ( int i=0; i<row; i++ ){
            for ( int j=0; j<col; j++ )
                s += values[i][j] + " ";

            s += "\n";
        }
        return s;
    }

    public static Matrix Zero ( int m, int n ){
        double[][] newValues = new double[m][n];

        for ( int i=0; i<m; i++ )
            for ( int j=0; j<n; j++ )
                newValues[i][j] = 0;

        return new Matrix ( newValues );
    }
}
