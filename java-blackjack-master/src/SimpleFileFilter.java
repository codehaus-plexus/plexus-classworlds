import java.io.File;
import javax.swing.filechooser.*;

public class SimpleFileFilter extends FileFilter
{
    private String extension = "";
    private String description = "";
    
    public SimpleFileFilter()
    {
        
    }
    
    public SimpleFileFilter(String extension, String description)
    {
        if (extension != null)
        {
            this.extension = extension;
        }
        
        if (description != null)
        {
            this.description = description;
        }
    }
    
    public boolean accept(File file)
    {
        if (file != null)
        {
            if (file.isDirectory())
            {
                return false;
            }
        
            String fileName = file.getName();
            int i = fileName.lastIndexOf('.');
            if (i > 0 && i < fileName.length() - 1)
            {
                String end = fileName.substring(i, fileName.length());
                return end.equals(extension);
            }
        }
        
        return false;
    }
    
    public String getDescription()
    {
        return description;
    }
}