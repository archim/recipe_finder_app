package com.example.recipe;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

public class FindRecipeServletTest extends Mockito {

    @Test
    public void testServlet() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        Part mockPart = mock(Part.class);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getPart("recipesFile")).thenReturn(mockPart);
        when(request.getPart("availableItemsFile")).thenReturn(mockPart);
        when(mockPart.getHeader(anyString())).thenReturn("filename=header.txt");
        try {
            new FindRecipeServlet().doPost(request, response);
        }catch (Exception exc){
            Assert.assertEquals(exc.getMessage(), "Unable to read file, please make sure it is in correct format");
        }


        //TODO: need to figure out how to mock InputStreamReader etc.
    }

}