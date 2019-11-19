/**
 * 
 */
package br.unicamp.ic.ginicoefficient.controller;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Daniel R. F. Apolinario
 *
 */
@RestController
public class GiniCoefficientController {

	@Autowired
	private Environment environment;

	@RequestMapping(value = "/calculate-gini-index/{dataSeries}", method = RequestMethod.GET)
	@ResponseBody
	// @GetMapping("/gini-coefficient/")
	public GiniCoefficientResult calculateGiniCoefficient(@PathVariable List<BigDecimal> dataSeries) {

		GiniCoefficientResult giniCoefficient = new GiniCoefficientResult();
		// set the port number of the server responding the request
		giniCoefficient.setPort(Integer.parseInt(environment.getProperty("local.server.port")));

		// sort the dataSeries in ascending order
		Collections.sort(dataSeries);

		giniCoefficient.setGiniIndex(calculateGiniIndex(dataSeries));

		return giniCoefficient;
	}

	/**
	 * @param dataSeries
	 * @return
	 */
	private BigDecimal calculateGiniIndex(List<BigDecimal> dataSeries) {
		BigDecimal firstSum = calculateFirstSum(dataSeries);
		BigDecimal secondSum = calculateSecondSum(dataSeries);
		BigDecimal giniIndexResult = new BigDecimal(0);
		if (firstSum.doubleValue() > 0 & secondSum.doubleValue() > 0) {
			giniIndexResult = firstSum.divide(secondSum, MathContext.DECIMAL32);
		}
		return giniIndexResult;
	}

	/**
	 * @param dataSeries
	 * @return
	 */
	private BigDecimal calculateSecondSum(List<BigDecimal> dataSeries) {
		BigDecimal sum = new BigDecimal(0);
		int totalElements = dataSeries.size();
		for (int i = 0; i < totalElements; i++) {
			sum = sum.add(dataSeries.get(i));
		}
		sum = sum.multiply(new BigDecimal(totalElements));
		return sum;
	}

	/**
	 * @param dataSeries
	 * @return
	 */
	private BigDecimal calculateFirstSum(List<BigDecimal> dataSeries) {
		int totalElements = dataSeries.size();
		BigDecimal sum = new BigDecimal(0);
		for (int i = 0; i < totalElements; i++) {
			sum = sum.add(dataSeries.get(i).multiply(new BigDecimal((2 * (i + 1)) - totalElements - 1)));
		}
		return sum;
	}
}
